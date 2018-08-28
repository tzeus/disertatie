package com.tudoreloprisan.tradingAPI.marketData;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;

public interface PipJumpCutOffCalculator<T> {

	Double calculatePipJumpCutOff(TradeableInstrument<T> instrument);
}
