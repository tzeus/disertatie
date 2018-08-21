package brokerAPI.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONObject;

import com.google.common.base.Preconditions;

import brokerAPI.account.BrokerConstants;
import brokerAPI.events.AccountEventPayLoad;
import brokerAPI.events.AccountEvents;
import brokerAPI.events.OrderEventPayLoad;
import brokerAPI.events.OrderEvents;
import tradingAPI.events.Event;
import tradingAPI.events.EventPayLoad;
import tradingAPI.order.OrderType;
import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;

public class BrokerUtils {
	private BrokerUtils() {
	}

	public static final BasicHeader createAuthHeader(String accessToken) {
		return new BasicHeader("Authorization", "Bearer " + accessToken);
	}

	public static String[] splitCcyPair(String instrument) {
		return TradingUtils.splitCcyPair(instrument, BrokerConstants.CCY_PAIR_SEP);
	}

	public static final String toBrokerCcy(String baseCcy, String quoteCcy) {
		final int expectedLen = 3;
		if (!StringUtils.isEmpty(baseCcy) && !StringUtils.isEmpty(quoteCcy) && baseCcy.length() == expectedLen
				&& quoteCcy.length() == expectedLen) {
			return String.format("%s%s%s", baseCcy, BrokerConstants.CCY_PAIR_SEP, quoteCcy);
		}
		throw new IllegalArgumentException(String.format(
				"base currency and quote currency cannot be null or empty" + " and must be %d char length",
				expectedLen));
	}

	public static final String isoCcyToBrokerCcy(String ccy) {
		final int expectedLen = 6;
		if (!StringUtils.isEmpty(ccy) && ccy.length() == expectedLen) {
			return String.format("%s%s%s", ccy.substring(0, 3), BrokerConstants.CCY_PAIR_SEP, ccy.substring(3));
		}
		throw new IllegalArgumentException(
				String.format("expected a string with length = %d but got %s", expectedLen, ccy));
	}

	public static final String brokerToHashTagCcy(String brokerCcy) {
		String[] currencies = BrokerUtils.splitCcyPair(brokerCcy);
		final String instrumentAsHashtag = TradingConstants.HASHTAG + currencies[0] + currencies[1];
		return instrumentAsHashtag;
	}

	public static final String hashTagCcyToBrokerCcy(String ccy) {
		final int expectedLen = TradingUtils.CCY_PAIR_LEN;
		if (!StringUtils.isEmpty(ccy) && ccy.startsWith(TradingConstants.HASHTAG) && ccy.length() == expectedLen) {

			return isoCcyToBrokerCcy(ccy.substring(1));
		}
		throw new IllegalArgumentException(
				String.format("expected a string with length = %d beginning with %s but got %s", expectedLen,
						TradingConstants.HASHTAG, ccy));
	}

	public static OrderType toOrderType(String type) {
		if (BrokerConstants.ORDER_MARKET.equals(type)) {
			return OrderType.MARKET;
		} else if (BrokerConstants.ORDER_LIMIT.equals(type) || BrokerConstants.ORDER_MARKET_IF_TOUCHED.equals(type)) {
			return OrderType.LIMIT;
		} else if (BrokerConstants.STOP_LOSS.equals(type)) {
			return OrderType.STOP_LOSS;
		} else if (BrokerConstants.TAKE_PROFIT.equals(type)) {
			return OrderType.TAKE_PROFIT;
		} else {
			throw new IllegalArgumentException("Unsupported order type:" + type);
		}
	}

	public static EventPayLoad<JSONObject> toBrokerEventPayLoad(String transactionType, JSONObject payLoad) {
		Preconditions.checkNotNull(transactionType);
		Event evt = findAppropriateType(AccountEvents.values(), transactionType);
		if (evt == null) {
			evt = findAppropriateType(OrderEvents.values(), transactionType);
			if (evt == null) {
				return null;
			} else {
				return new OrderEventPayLoad((OrderEvents) evt, payLoad);
			}
		} else {
			return new AccountEventPayLoad((AccountEvents) evt, payLoad);
		}
	}

	private static final Event findAppropriateType(Event[] events, String transactionType) {
		for (Event evt : events) {
			if (evt.name().equals(transactionType)) {
				return evt;
			}
		}
		return null;
	}

}
