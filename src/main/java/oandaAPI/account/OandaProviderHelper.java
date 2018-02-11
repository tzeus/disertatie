package oandaAPI.account;

import oandaAPI.util.OandaUtils;
import tradingAPI.account.ProviderHelper;
import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;

public class OandaProviderHelper implements ProviderHelper<String> {

	@Override
	public String fromIsoFormat(String instrument) {
		return OandaUtils.isoCcyToOandaCcy(instrument);
	}

	@Override
	public String fromPairSeparatorFormat(String instrument) {
		String[] pair = TradingUtils.splitInstrumentPair(instrument);
		return String.format("%s%s%s", pair[0], OandaConstants.CCY_PAIR_SEP, pair[1]);
	}

	@Override
	public String toIsoFormat(String instrument) {
		String tokens[] = TradingUtils.splitCcyPair(instrument, TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE);
		String isoInstrument = tokens[0] + tokens[1];
		return isoInstrument;
	}

	@Override
	public String fromHashTagCurrency(String instrument) {
		return OandaUtils.hashTagCcyToOandaCcy(instrument);
	}

	@Override
	public String getLongNotation() {
		return OandaConstants.BUY;
	}

	@Override
	public String getShortNotation() {
		return OandaConstants.SELL;
	}

}
