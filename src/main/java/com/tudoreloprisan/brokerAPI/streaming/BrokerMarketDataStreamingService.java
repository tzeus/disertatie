package com.tudoreloprisan.brokerAPI.streaming;

import java.io.BufferedReader;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.tudoreloprisan.brokerAPI.account.BrokerConstants;
import com.tudoreloprisan.brokerAPI.account.BrokerJsonKeys;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatCallback;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.marketData.MarketEventCallback;
import com.tudoreloprisan.tradingAPI.streaming.MarketDataStreamingService;
import com.tudoreloprisan.tradingAPI.util.TradingConstants;
import com.tudoreloprisan.tradingAPI.util.TradingUtils;

public class BrokerMarketDataStreamingService extends BrokerStreamingService implements MarketDataStreamingService {

	private static final Logger					LOG	= Logger.getLogger(BrokerMarketDataStreamingService.class);
	private final String						url;
	private final MarketEventCallback<String>	marketEventCallback;

	public BrokerMarketDataStreamingService(String url, String accessToken, String accountId,
											Collection<TradeableInstrument<String>> instruments, MarketEventCallback<String> marketEventCallback,
											HeartBeatCallback<DateTime> heartBeatCallback, String heartbeatSourceId) {
		super(accessToken, heartBeatCallback, heartbeatSourceId);
		this.url = url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
				+ BrokerConstants.PRICES_RESOURCE + "?instruments="
				+ instrumentsAsCsv(instruments)+TradingConstants.ENCODED_COMMA+"USD_CAD"  ;
		this.marketEventCallback = marketEventCallback;
	}

	private String instrumentsAsCsv(Collection<TradeableInstrument<String>> instruments) {
		StringBuilder csvLst = new StringBuilder();
		boolean firstTime = true;
		for (TradeableInstrument<String> instrument : instruments) {
			if (firstTime) {
				firstTime = false;
			} else {
				csvLst.append(TradingConstants.ENCODED_COMMA);
			}
			csvLst.append(instrument.getInstrument());
		}
		return csvLst.toString();
	}

	@Override
	protected String getStreamingUrl() {
		return this.url;
	}

	@Override
	public void stopMarketDataStreaming() {
		this.serviceUp = false;
		if (streamThread != null && streamThread.isAlive()) {
			streamThread.interrupt();
		}
	}

	@Override
	public void startMarketDataStreaming() {
		stopMarketDataStreaming();
		this.streamThread = new Thread(() -> {
			CloseableHttpClient httpClient = getHttpClient();
			try {
				BufferedReader br = setUpStreamIfPossible(httpClient);
				if (br != null) {
					String line;
					while ((line = br.readLine()) != null && serviceUp) {

						JsonObject instrumentTick = new Gson().fromJson(line, JsonObject.class);


						if (instrumentTick.get(BrokerJsonKeys.TYPE.value()).getAsString().equals(BrokerJsonKeys.PRICE.toString())) {
							final String instrument = instrumentTick.get(BrokerJsonKeys.INSTRUMENT.value()).toString();
							String dateTimeAsString = instrumentTick.get(BrokerJsonKeys.TIME.value()).getAsString();
							int lastDot = dateTimeAsString.lastIndexOf('.');
							dateTimeAsString = dateTimeAsString.substring(0, lastDot);
							DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
							DateTime eventTime = DateTime.parse(dateTimeAsString, formatter);
							final double bidPrice = findPrice(instrumentTick, BrokerJsonKeys.BIDS.value());
							final double askPrice = findPrice(instrumentTick, BrokerJsonKeys.ASKS.value());
							marketEventCallback.onMarketEvent(new TradeableInstrument<String>(instrument),
									bidPrice, askPrice, eventTime);
						} else if (instrumentTick.get(BrokerJsonKeys.TYPE.value()).equals(BrokerJsonKeys.HEARTBEAT.toString())) {
							handleHeartBeat(instrumentTick);
						}
						else {
							handleDisconnect(line);
						}
					}
					br.close();
					// stream.close();
				}
			} catch (Exception e) {
				LOG.error("error encountered inside market data streaming thread", e);
			} finally {
				serviceUp = false;
				TradingUtils.closeSilently(httpClient);
			}

		}, "BrokerMarketDataStreamingThread");
		this.streamThread.start();

	}

	@Override
	protected void startStreaming() {
		startMarketDataStreaming();

	}

	@Override
	protected void stopStreaming() {
		stopMarketDataStreaming();

	}

	private double findPrice(JsonObject instrumentTick, String keyName) {
		JsonArray bidArray = instrumentTick.get(keyName).getAsJsonArray();
		for (Object bid : bidArray) {
			JsonObject jsonBid = (JsonObject) bid;
			int liquidity = jsonBid.get(BrokerJsonKeys.LIQUIDITY.value()).getAsInt();
			if (liquidity > 0) {
				return jsonBid.get(BrokerJsonKeys.PRICE.value()).getAsDouble();
			}
		}
		return 0;
	}

}
