package com.tudoreloprisan.tradingAPI.marketData;

import org.joda.time.DateTime;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;

public interface MarketEventCallback <T>{

	void onMarketEvent(TradeableInstrument<T> ti, double bid,
			double ask, DateTime eventDate);
}
