package tradingAPI.market;

import java.util.Collection;
import java.util.Map;

import tradingAPI.instruments.TradeableInstrument;

public interface CurrentPriceInfoProvider<T> {

	Map<TradeableInstrument<T>, Price<T>> getCurrentPricesForInstruments(
			Collection<TradeableInstrument<T>> instruments, String accountID);
}
