package com.tudoreloprisan.tradingAPI.order;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import com.tudoreloprisan.tradingAPI.account.AccountInfoService;
import com.tudoreloprisan.tradingAPI.account.BaseTradingConfig;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.market.CurrentPriceInfoProvider;
import com.tudoreloprisan.tradingAPI.market.Price;
import com.tudoreloprisan.tradingAPI.trade.TradingDecision;
import com.tudoreloprisan.tradingAPI.trade.TradingSignal;
import org.springframework.stereotype.Component;

public class OrderExecutionService<M, N, K> implements Runnable {

	private static final Logger LOG = Logger.getLogger(OrderExecutionService.class);

	private final BlockingQueue<TradingDecision<N>> orderQueue;
	private final AccountInfoService<K, N> accountInfoService;
	private final OrderManagementProvider<M, N, K> orderManagementProvider;
	private final BaseTradingConfig baseTradingConfig;
	private final PreOrderValidationService<M, N, K> preOrderValidationService;
	private final CurrentPriceInfoProvider<N, K> currentPriceInfoProvider;
	private volatile boolean serviceUp = true;
	Thread orderExecThread;

	public OrderExecutionService(BlockingQueue<TradingDecision<N>> orderQueue,
			AccountInfoService<K, N> accountInfoService, OrderManagementProvider<M, N, K> orderManagementProvider,
			BaseTradingConfig baseTradingConfig, PreOrderValidationService<M, N, K> preOrderValidationService,
			CurrentPriceInfoProvider<N, K> currentPriceInfoProvider) {
		this.orderQueue = orderQueue;
		this.accountInfoService = accountInfoService;
		this.orderManagementProvider = orderManagementProvider;
		this.baseTradingConfig = baseTradingConfig;
		this.preOrderValidationService = preOrderValidationService;
		this.currentPriceInfoProvider = currentPriceInfoProvider;
	}

	@PostConstruct
	public void init() {
		orderExecThread = new Thread(this, this.getClass().getSimpleName());
		orderExecThread.start();
	}

	@PreDestroy
	public void shutDown() {
		this.serviceUp = false;
	}

	private boolean preValidate(TradingDecision<N> decision, K accountId) {
		if (TradingSignal.NONE != decision.getSignal()
				&& this.preOrderValidationService.checkInstrumentNotAlreadyTraded(decision.getInstrument())
				&& this.preOrderValidationService.checkLimitsForCcy(decision.getInstrument(), decision.getSignal())) {
			Collection<TradeableInstrument<N>> instruments = Lists.newArrayList();
			instruments.add(decision.getInstrument());
			Map<TradeableInstrument<N>, Price<N>> priceMap = this.currentPriceInfoProvider
					.getCurrentPricesForInstruments(instruments, accountId);
			if (priceMap.containsKey(decision.getInstrument())) {
				Price<N> currentPrice = priceMap.get(decision.getInstrument());
				return this.preOrderValidationService.isInSafeZone(decision.getSignal(),
						decision.getSignal() == TradingSignal.LONG ? currentPrice.getAskPrice() : currentPrice
								.getBidPrice(), decision.getInstrument());
			}
		}
		return false;
	}

	@Override
	public void run() {
		while (serviceUp) {
			try {
				Collection<K> accountIds = this.accountInfoService.findAccountsToTrade();
				if (accountIds.isEmpty()) {
					LOG.info("Not a single eligible account found as the reserve may have been exhausted.");
					continue;
				}
				
				K accountId = null;
				for (K accountIdinCollection : accountIds) {
					accountId = accountIdinCollection;
				}
				
				TradingDecision<N> decision = this.orderQueue.take();
				if (!preValidate(decision, accountId)) {
					continue;
				}
				
				Order<N, M> order = null;
				if (decision.getLimitPrice() == 0.0) {// market order
					order = new Order<N, M>(decision.getInstrument(), String.valueOf(this.baseTradingConfig.getMaxAllowedQuantity()),
							decision.getSignal(), OrderType.MARKET, decision.getTakeProfitPrice(), decision
									.getStopLossPrice());
				} else {
					order = new Order<N, M>(decision.getInstrument(), String.valueOf(this.baseTradingConfig.getMaxAllowedQuantity()),
							decision.getSignal(), OrderType.LIMIT, decision.getTakeProfitPrice(), decision
									.getStopLossPrice(), decision.getLimitPrice());
				}
				for (K accountIdinCollection : accountIds) {
					M orderId = this.orderManagementProvider.placeOrder(order, accountIdinCollection);
					if (orderId != null) {
						order.setOrderId(orderId);
					}
					break;
				}
			} catch (Exception e) {
				LOG.error("error encountered inside order execution service", e);
			}
		}

	}

}
