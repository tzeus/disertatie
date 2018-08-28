package com.tudoreloprisan.brokerAPI.marketData;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
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
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				JSONArray candlsticks = (JSONArray) jsonResp.get(BrokerJsonKeys.CANDLES.value());

				for (Object o : candlsticks) {
					JSONObject candlestick = (JSONObject) o;
					JSONObject candlestickPrices = (JSONObject) candlestick.get(BrokerJsonKeys.MIDPOINT.value());

					final double openPrice = Double.valueOf((String) candlestickPrices.get(BrokerJsonKeys.OPEN.value()));
					final double highPrice = Double.valueOf((String) candlestickPrices.get(BrokerJsonKeys.HIGH.value()));
					final double lowPrice = Double.valueOf((String) candlestickPrices.get(BrokerJsonKeys.LOW.value()));
					final double closePrice = Double.valueOf((String) candlestickPrices.get(BrokerJsonKeys.CLOSE.value()));
					DateTime timestamp = DateTime.parse((String) candlestick.get(BrokerJsonKeys.TIME.value()));
					CandleStick<String> candle = new CandleStick<String>(openPrice, highPrice, lowPrice, closePrice,
							timestamp, instrument, granularity);
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
