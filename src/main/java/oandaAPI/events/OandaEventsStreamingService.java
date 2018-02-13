package oandaAPI.events;

import java.io.BufferedReader;
import java.util.Collection;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import oandaAPI.account.OandaConstants;
import oandaAPI.account.OandaJsonKeys;
import oandaAPI.streaming.OandaStreamingService;
import oandaAPI.util.OandaUtils;
import tradingAPI.account.Account;
import tradingAPI.account.AccountDataProvider;
import tradingAPI.events.EventCallback;
import tradingAPI.events.EventPayLoad;
import tradingAPI.events.EventsStreamingService;
import tradingAPI.heartbeat.HeartBeatCallback;
import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;

public class OandaEventsStreamingService extends OandaStreamingService implements EventsStreamingService {

	private static final Logger					LOG	= Logger.getLogger(OandaEventsStreamingService.class);
	private final String						url;
	private final AccountDataProvider<String>	accountDataProvider;
	private final EventCallback<JSONObject>		eventCallback;

	public OandaEventsStreamingService(final String url, final String accessToken,
			AccountDataProvider<String> accountDataProvider, EventCallback<JSONObject> eventCallback,
			HeartBeatCallback<DateTime> heartBeatCallback, String heartBeatSourceId) {
		super(accessToken, heartBeatCallback, heartBeatSourceId);
		this.url = url;
		this.accountDataProvider = accountDataProvider;
		this.eventCallback = eventCallback;
	}

	@Override
	public void stopEventsStreaming() {
		this.serviceUp = false;
		if (streamThread != null && streamThread.isAlive()) {
			streamThread.interrupt();
		}
	}

	@Override
	protected String getStreamingUrl() {
		Collection<Account<String>> accounts = accountDataProvider.getLatestAccountsInfo();
		String accountId = "";
		for(Account<String> account: accounts) {
			accountId = account.getAccountId();
		}		
		return this.url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId +
				OandaConstants.TRANSACTIONS_RESOURCE;
	}

	@Override
	public void startEventsStreaming() {
		stopEventsStreaming();
		streamThread = new Thread(new Runnable() {

			@Override
			public void run() {
				CloseableHttpClient httpClient = getHttpClient();
				try {
					BufferedReader br = setUpStreamIfPossible(httpClient);
					if (br != null) {
						String line;
						while ((line = br.readLine()) != null && serviceUp) {
							
							Object obj = JSONValue.parse(line);
							JSONObject jsonPayLoad = (JSONObject) obj;
							if (jsonPayLoad.get(OandaJsonKeys.TYPE.value()).equals(OandaJsonKeys.HEARTBEAT.toString())) {
								handleHeartBeat(jsonPayLoad);
							} else {
								
								String transactionType = (String) jsonPayLoad.get(OandaJsonKeys.TYPE.value());
								
								EventPayLoad<JSONObject> payLoad = OandaUtils.toOandaEventPayLoad(transactionType,
										jsonPayLoad);
								if (payLoad != null) {
									eventCallback.onEvent(payLoad);
								}
							} 
						}
						
					}

				} catch (Exception e) {
					LOG.error("error encountered inside event streaming thread", e);
				} finally {
					serviceUp = false;
					TradingUtils.closeSilently(httpClient);
					
				}

			}
		}, "OandEventStreamingThread");
		streamThread.start();
	}

	@Override
	protected void startStreaming() {
		this.startEventsStreaming();
	}

	@Override
	protected void stopStreaming() {
		this.stopEventsStreaming();
	}

}
