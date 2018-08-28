package com.tudoreloprisan.tradingAPI.marketData;

import java.util.List;

import org.joda.time.DateTime;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;

public interface HistoricMarketDataProvider<T> {

	
	List<CandleStick<T>> getCandleSticks(TradeableInstrument<T> instrument, CandleStickGranularity granularity,
			DateTime from, DateTime to);

	
	List<CandleStick<T>> getCandleSticks(TradeableInstrument<T> instrument, CandleStickGranularity granularity,
			int count);
}
