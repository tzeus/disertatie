package tradingAPI.marketData;

import tradingAPI.instruments.TradeableInstrument;

public interface PipJumpCutOffCalculator<T> {

	Double calculatePipJumpCutOff(TradeableInstrument<T> instrument);
}
