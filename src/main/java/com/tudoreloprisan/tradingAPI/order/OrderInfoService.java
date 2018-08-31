package com.tudoreloprisan.tradingAPI.order;

import java.util.Collection;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.util.TradingUtils;

public class OrderInfoService<M, N, K> {

	private final OrderManagementProvider<M, N, K> orderManagementProvider;

	public OrderInfoService(OrderManagementProvider<M, N, K> orderManagementProvider) {
		this.orderManagementProvider = orderManagementProvider;
	}

	public Collection<Order<N, M>> allPendingOrders() {
		return this.orderManagementProvider.allPendingOrders();
	}

	public Collection<Order<N, M>> pendingOrdersForAccount(K accountId) {
		return this.orderManagementProvider.pendingOrdersForAccount(accountId);
	}

	public Collection<Order<N, M>> pendingOrdersForInstrument(TradeableInstrument<N> instrument) {
		return this.orderManagementProvider.pendingOrdersForInstrument(instrument);
	}

	public Order<N, M> pendingOrderForAccount(M orderId, K accountId) {
		return this.orderManagementProvider.pendingOrderForAccount(orderId, accountId);
	}

	public int findNetPositionCountForCurrency(String currency) {
		Collection<Order<N, M>> allOrders = allPendingOrders();
		int positionCount = 0;
		for (Order<N, M> order : allOrders) {			
			positionCount += TradingUtils.getSign(order.getInstrument().getInstrument(), order.getSide(), currency);
		}
		return positionCount;
	}


	public Collection<Order<String, String>> getAllOrders(String accountId) {
		return this.orderManagementProvider.getAllOrders(accountId);
	}
}
