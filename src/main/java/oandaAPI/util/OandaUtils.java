package oandaAPI.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicHeader;

import oandaAPI.account.OandaConstants;
import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;

public class OandaUtils {
	private OandaUtils() {
	}

	public static final BasicHeader createAuthHeader(String accessToken) {
		return new BasicHeader("Authorization", "Bearer " + accessToken);
	}

	public static String[] splitCcyPair(String instrument) {
		return TradingUtils.splitCcyPair(instrument, OandaConstants.CCY_PAIR_SEP);
	}

	public static final String toOandaCcy(String baseCcy, String quoteCcy) {
		final int expectedLen = 3;
		if (!StringUtils.isEmpty(baseCcy) && !StringUtils.isEmpty(quoteCcy) && baseCcy.length() == expectedLen
				&& quoteCcy.length() == expectedLen) {
			return String.format("%s%s%s", baseCcy, OandaConstants.CCY_PAIR_SEP, quoteCcy);
		}
		throw new IllegalArgumentException(String.format(
				"base currency and quote currency cannot be null or empty" + " and must be %d char length",
				expectedLen));
	}

	public static final String isoCcyToOandaCcy(String ccy) {
		final int expectedLen = 6;
		if (!StringUtils.isEmpty(ccy) && ccy.length() == expectedLen) {
			return String.format("%s%s%s", ccy.substring(0, 3), OandaConstants.CCY_PAIR_SEP, ccy.substring(3));
		}
		throw new IllegalArgumentException(
				String.format("expected a string with length = %d but got %s", expectedLen, ccy));
	}

	public static final String oandaToHashTagCcy(String oandaCcy) {
		String[] currencies = OandaUtils.splitCcyPair(oandaCcy);
		final String instrumentAsHashtag = TradingConstants.HASHTAG + currencies[0] + currencies[1];
		return instrumentAsHashtag;
	}

	public static final String hashTagCcyToOandaCcy(String ccy) {
		final int expectedLen = TradingUtils.CCY_PAIR_LEN;
		if (!StringUtils.isEmpty(ccy) && ccy.startsWith(TradingConstants.HASHTAG) && ccy.length() == expectedLen) {

			return isoCcyToOandaCcy(ccy.substring(1));
		}
		throw new IllegalArgumentException(
				String.format("expected a string with length = %d beginning with %s but got %s", expectedLen,
						TradingConstants.HASHTAG, ccy));
	}

}
