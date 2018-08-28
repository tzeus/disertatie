package com.tudoreloprisan.strategy;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import com.tudoreloprisan.tradingAPI.account.TradingConfig;
import com.tudoreloprisan.tradingAPI.instruments.InstrumentService;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.marketData.MarketDataPayLoad;
import com.tudoreloprisan.tradingAPI.marketData.PipJumpCutOffCalculator;
import com.tudoreloprisan.tradingAPI.trade.TradingDecision;
import com.tudoreloprisan.tradingAPI.trade.TradingSignal;

@TradingStrategy
public class FadeTheMoveStrategy<T> {

	@Autowired
	TradingConfig tradingConfig;
	@Autowired
	InstrumentService<T> instrumentService;
	@Autowired
	PipJumpCutOffCalculator<T> pipJumpCutOffCalculator;
	@Resource(name = "orderQueue")
	BlockingQueue<TradingDecision<T>> orderQueue;
	private final Collection<TradeableInstrument<T>> instruments;

	private final Map<TradeableInstrument<T>, Cache<DateTime, MarketDataPayLoad<T>>> instrumentRecentPricesCache = Maps
			.newHashMap();

	public FadeTheMoveStrategy(Collection<TradeableInstrument<T>> instruments) {
		this.instruments = instruments;
	}

	@PostConstruct
	public void init() {
		for (TradeableInstrument<T> instrument : instruments) {
			Cache<DateTime, MarketDataPayLoad<T>> recentPricesCache = CacheBuilder.newBuilder()
					.expireAfterWrite(tradingConfig.getFadeTheMovePriceExpiry(), TimeUnit.MINUTES)
					.<DateTime, MarketDataPayLoad<T>> build();
			instrumentRecentPricesCache.put(instrument, recentPricesCache);
		}
	}

	@Subscribe
	@AllowConcurrentEvents
	public void handleMarketDataEvent(MarketDataPayLoad<T> marketDataPayLoad) {
		if (instrumentRecentPricesCache.containsKey(marketDataPayLoad.getInstrument())) {
			instrumentRecentPricesCache.get(marketDataPayLoad.getInstrument()).put(marketDataPayLoad.getEventDate(),
					marketDataPayLoad);
		}
	}

	// called by scheduler
	public void analysePrices() {
		for (Map.Entry<TradeableInstrument<T>, Cache<DateTime, MarketDataPayLoad<T>>> entry : instrumentRecentPricesCache
				.entrySet()) {
			SortedMap<DateTime, MarketDataPayLoad<T>> sortedByDate = ImmutableSortedMap
					.copyOf(entry.getValue().asMap());
			if (sortedByDate.isEmpty()) {
				continue;
			}
			Double pipJump = calculatePipJump(sortedByDate.values(), entry.getKey());
			Double absPipJump = Math.abs(pipJump);
			if (absPipJump >= this.pipJumpCutOffCalculator.calculatePipJumpCutOff(entry.getKey())) {
				MarketDataPayLoad<T> lastPayLoad = sortedByDate.get(sortedByDate.lastKey());
				Double pip = this.instrumentService.getPipForInstrument(entry.getKey());
				double takeProfitPrice;
				double limitPrice;
				TradingSignal signal = null;
				if (Math.signum(pipJump) > 0) {// Short
					signal = TradingSignal.SHORT;
					limitPrice = lastPayLoad.getBidPrice() + tradingConfig.getFadeTheMoveDistanceToTrade() * pip;
					takeProfitPrice = limitPrice - tradingConfig.getFadeTheMovePipsDesired() * pip;
				} else {
					signal = TradingSignal.LONG;
					limitPrice = lastPayLoad.getAskPrice() - tradingConfig.getFadeTheMoveDistanceToTrade() * pip;
					takeProfitPrice = limitPrice + tradingConfig.getFadeTheMovePipsDesired() * pip;
				}
				this.orderQueue.offer(new TradingDecision<T>(entry.getKey(), signal, takeProfitPrice, 0.0, limitPrice,
						TradingDecision.SRCDECISION.FADE_THE_MOVE));
				entry.getValue().asMap()
						.clear();/*
									 * clear the prices so that we do not keep
									 * working on old decision
									 */
			}
		}
	}

	private double calculatePipJump(Collection<MarketDataPayLoad<T>> prices, TradeableInstrument<T> instrument) {
		List<MarketDataPayLoad<T>> priceList = Lists.newArrayList(prices);
		MarketDataPayLoad<T> startPrice = priceList.get(0);
		MarketDataPayLoad<T> lastPrice = priceList.get(priceList.size() - 1);
		Double pip = this.instrumentService.getPipForInstrument(instrument);
		Double pipJump = (lastPrice.getBidPrice() - startPrice.getBidPrice()) / pip;
		return pipJump;
	}
}
