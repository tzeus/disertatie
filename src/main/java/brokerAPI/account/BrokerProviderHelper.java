package brokerAPI.account;

import brokerAPI.util.BrokerUtils;
import tradingAPI.account.ProviderHelper;
import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;

public class BrokerProviderHelper implements ProviderHelper<String> {

	@Override
	public String fromIsoFormat(String instrument) {
		return BrokerUtils.isoCcyToBrokerCcy(instrument);
	}

	@Override
	public String fromPairSeparatorFormat(String instrument) {
		String[] pair = TradingUtils.splitInstrumentPair(instrument);
		return String.format("%s%s%s", pair[0], BrokerConstants.CCY_PAIR_SEP, pair[1]);
	}

	@Override
	public String toIsoFormat(String instrument) {
		String tokens[] = TradingUtils.splitCcyPair(instrument, TradingConstants.CURRENCY_PAIR_SEP_UNDERSCORE);
		String isoInstrument = tokens[0] + tokens[1];
		return isoInstrument;
	}

	@Override
	public String fromHashTagCurrency(String instrument) {
		return BrokerUtils.hashTagCcyToBrokerCcy(instrument);
	}

	@Override
	public String getLongNotation() {
		return BrokerConstants.BUY;
	}

	@Override
	public String getShortNotation() {
		return BrokerConstants.SELL;
	}

}
