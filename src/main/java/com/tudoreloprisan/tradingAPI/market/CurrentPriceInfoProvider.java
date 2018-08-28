package com.tudoreloprisan.tradingAPI.market;

import java.util.Collection;
import java.util.Map;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;

public interface CurrentPriceInfoProvider<T, K> {

	Map<TradeableInstrument<T>, Price<T>> getCurrentPricesForInstruments(
			Collection<TradeableInstrument<T>> instruments, K accountId);
}
