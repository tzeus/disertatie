package com.tudoreloprisan.brokerAPI.marketData;

import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.Lists;

import com.tudoreloprisan.brokerAPI.account.BrokerConstants;
import com.tudoreloprisan.brokerAPI.account.BrokerJsonKeys;
import com.tudoreloprisan.brokerAPI.util.BrokerUtils;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.marketData.CandleStick;
import com.tudoreloprisan.tradingAPI.marketData.CandleStickGranularity;
import com.tudoreloprisan.tradingAPI.marketData.HistoricMarketDataProvider;
import com.tudoreloprisan.tradingAPI.util.TradingConstants;
import com.tudoreloprisan.tradingAPI.util.TradingUtils;

public class BrokerHistoricMarketDataProvider implements HistoricMarketDataProvider<String> {

	private final String		url;
	private final BasicHeader	authHeader;
	private static final String	tzGMT				= "GMT";
	private static final Logger	LOG					= Logger.getLogger(BrokerHistoricMarketDataProvider.class);
	static final int			LIMIT_ERR_CODE		= 36;
	static final int			MAX_CANDLES_COUNT	= 5000;

	public BrokerHistoricMarketDataProvider(String url, String accessToken) {
		this.url = url;
		this.authHeader = BrokerUtils.createAuthHeader(accessToken);
	}

	String getFromToUrl(TradeableInstrument<String> instrument, CandleStickGranularity granularity, DateTime from,
			DateTime to) {
		return String.format("%s%s%s%s%s?granularity=%s&alignmentTimezone=%s&from=%s&dailyAlignment=0&to=%s&price=M",
				this.url, BrokerConstants.INSTRUMENTS_RESOURCE_FOR_CANDLES, TradingConstants.FWD_SLASH, instrument.getInstrument(),
				BrokerConstants.CANDLES_RESOURCE, granularity.name(), tzGMT, from.toString(),
				to.toString());
	}

	String getFromCountUrl(TradeableInstrument<String> instrument, CandleStickGranularity granularity, DateTime from,
			int count) {
		return String.format("%s%s%s%s%s?granularity=%s&alignmentTimezone=%s&from=%s&dailyAlignment=0&price=M&count=%d",
				this.url, BrokerConstants.INSTRUMENTS_RESOURCE_FOR_CANDLES, TradingConstants.FWD_SLASH, instrument.getInstrument(),
				BrokerConstants.CANDLES_RESOURCE, granularity.name(), tzGMT, from.toString(), count);
	}

	String getToCountUrl(TradeableInstrument<String> instrument, CandleStickGranularity granularity, DateTime to,
			int count) {
		return String.format("%s%s%s%s%s?granularity=%s&alignmentTimezone=%s&to=%s&dailyAlignment=0&price=M&count=%d",
				this.url, BrokerConstants.INSTRUMENTS_RESOURCE_FOR_CANDLES, TradingConstants.FWD_SLASH, instrument.getInstrument(),
				BrokerConstants.CANDLES_RESOURCE, granularity.name(), tzGMT, to.toString(), count);
	}

	String getCountUrl(TradeableInstrument<String> instrument, CandleStickGranularity granularity, int count) {
		return String.format("%s%s%s%s%s?granularity=%s&alignmentTimezone=%s&dailyAlignment=0&price=M&count=%d",
				this.url, BrokerConstants.INSTRUMENTS_RESOURCE_FOR_CANDLES, TradingConstants.FWD_SLASH, instrument.getInstrument(),
				BrokerConstants.CANDLES_RESOURCE, granularity.name(), tzGMT, count);
	}

	@Override
	public List<CandleStick<String>> getCandleSticks(TradeableInstrument<String> instrument,
			CandleStickGranularity granularity, DateTime from, DateTime to) {
		return getCandleSticks(instrument, getFromToUrl(instrument, granularity, from, to), granularity);
	}

	@Override
	public List<CandleStick<String>> getCandleSticks(TradeableInstrument<String> instrument,
			CandleStickGranularity granularity, int count) {
		return getCandleSticks(instrument, getCountUrl(instrument, granularity, count), granularity);
	}

	private List<CandleStick<String>> getCandleSticks(TradeableInstrument<String> instrument, String url,
			CandleStickGranularity granularity) {
		List<CandleStick<String>> allCandleSticks = Lists.newArrayList();
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(url);
			httpGet.setHeader(authHeader);
			httpGet.setHeader(BrokerConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				JsonObject jsonResp = new GsonBuilder().disableHtmlEscaping().create().fromJson(strResp, JsonObject.class);
				JsonArray candlestickData = jsonResp.get(BrokerJsonKeys.CANDLES.value()).getAsJsonArray();

				for (Object o : candlestickData) {
					JsonObject candlestick = (JsonObject) o;
					JsonObject candlestickPrices = (JsonObject) candlestick.get(BrokerJsonKeys.MIDPOINT.value());

					final double openPrice =  candlestickPrices.get(BrokerJsonKeys.OPEN.value()).getAsDouble();
					final double highPrice = candlestickPrices.get(BrokerJsonKeys.HIGH.value()).getAsDouble();
					final double lowPrice = candlestickPrices.get(BrokerJsonKeys.LOW.value()).getAsDouble();
					final double closePrice = candlestickPrices.get(BrokerJsonKeys.CLOSE.value()).getAsDouble();
					final long volume = candlestick.get(BrokerJsonKeys.VOLUME.value()).getAsLong();

//					String createTimeAsString = candlestick.get(BrokerJsonKeys.TIME.value()).getAsString();
//
//					int lastDot = createTimeAsString.lastIndexOf('.');
//					createTimeAsString = createTimeAsString.substring(0, lastDot).replace('T', ' ');
//					DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd' 'HH:mm:ss");
//					DateTime createTime = formatter.parseDateTime(createTimeAsString);

					DateTime timestamp = DateTime.parse(candlestick.get(BrokerJsonKeys.TIME.value()).getAsString());
					CandleStick<String> candle = new CandleStick<String>(openPrice, highPrice, lowPrice, closePrice,
							timestamp, instrument, granularity, volume);
					allCandleSticks.add(candle);
				}
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception e) {
			LOG.error(e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return allCandleSticks;
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}
}
