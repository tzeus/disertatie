package tradingAPI.instruments;

import java.util.Collection;

public interface InstrumentDataProvider<T> {
	/**
	 * 
	 * @return a collection of all TradeableInstrument available to trade on the
	 *         brokerage platform.
	 */
	Collection<TradeableInstrument<T>> getInstruments();
}
