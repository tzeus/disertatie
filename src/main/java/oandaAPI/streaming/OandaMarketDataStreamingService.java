package oandaAPI.streaming;

import java.io.BufferedReader;
import java.util.Collection;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import oandaAPI.account.OandaConstants;
import oandaAPI.account.OandaJsonKeys;
import tradingAPI.heartbeat.HeartBeatCallback;
import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.marketData.MarketEventCallback;
import tradingAPI.streaming.MarketDataStreamingService;
import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;

public class OandaMarketDataStreamingService extends OandaStreamingService implements MarketDataStreamingService {

	private static final Logger					LOG	= Logger.getLogger(OandaMarketDataStreamingService.class);
	private final String						url;
	private final MarketEventCallback<String>	marketEventCallback;

	public OandaMarketDataStreamingService(String url, String accessToken, String accountId,
			Collection<TradeableInstrument<String>> instruments, MarketEventCallback<String> marketEventCallback,
			HeartBeatCallback<DateTime> heartBeatCallback, String heartbeatSourceId) {
		super(accessToken, heartBeatCallback, heartbeatSourceId);
		this.url = url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
				+ TradingConstants.FWD_SLASH + OandaConstants.PRICES_RESOURCE + "?instruments="
				+ instrumentsAsCsv(instruments);
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
		this.streamThread = new Thread(new Runnable() {

			@Override
			public void run() {
				CloseableHttpClient httpClient = getHttpClient();
				try {
					BufferedReader br = setUpStreamIfPossible(httpClient);
					if (br != null) {
						String line;
						while ((line = br.readLine()) != null && serviceUp) {
							Object obj = JSONValue.parse(line);
							JSONObject instrumentTick = (JSONObject) obj;
							
							
							if (instrumentTick.get(OandaJsonKeys.TYPE.value()).equals(OandaJsonKeys.PRICE.toString())) {
								final String instrument = instrumentTick.get(OandaJsonKeys.INSTRUMENT.value()).toString();
								final String timeAsString = instrumentTick.get(OandaJsonKeys.TIME.value()).toString();								
								DateTime eventTime = DateTime.parse(timeAsString);
								final double bidPrice = findPrice(instrumentTick, OandaJsonKeys.BIDS.value());
								final double askPrice = findPrice(instrumentTick, OandaJsonKeys.ASKS.value());
								marketEventCallback.onMarketEvent(new TradeableInstrument<String>(instrument),
										bidPrice, askPrice, eventTime);
							} else if (instrumentTick.get(OandaJsonKeys.TYPE.value()).equals(OandaJsonKeys.HEARTBEAT.toString())) {
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

			}
		}, "OandMarketDataStreamingThread");
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

	private double findPrice(JSONObject instrumentTick, String keyName) {
		JSONArray bidArray = (JSONArray) instrumentTick.get(keyName);
		for (Object bid : bidArray) {			
			JSONObject jsonBid = (JSONObject) bid;			
			int liquidity = (int)(long) jsonBid.get(OandaJsonKeys.LIQUIDITY.value());
			if (liquidity > 0) {
				return Double.valueOf((String) jsonBid.get(OandaJsonKeys.PRICE.value()));
			}
		}
		return 0;
	}

}
