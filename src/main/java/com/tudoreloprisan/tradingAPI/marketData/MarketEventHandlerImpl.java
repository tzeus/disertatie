package com.tudoreloprisan.tradingAPI.marketData;

import org.joda.time.DateTime;

import com.google.common.eventbus.EventBus;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;

public class MarketEventHandlerImpl<T> implements MarketEventCallback<T> {

	private final EventBus eventBus;

	public MarketEventHandlerImpl(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void onMarketEvent(TradeableInstrument<T> instrument, double bid, double ask, DateTime eventDate) {
		MarketDataPayLoad<T> payload = new MarketDataPayLoad<T>(instrument, bid, ask, eventDate);
		eventBus.post(payload);

	}

}
