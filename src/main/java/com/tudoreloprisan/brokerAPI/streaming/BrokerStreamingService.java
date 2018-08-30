package com.tudoreloprisan.brokerAPI.streaming;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;

import com.tudoreloprisan.brokerAPI.account.BrokerConstants;
import com.tudoreloprisan.brokerAPI.account.BrokerJsonKeys;
import com.tudoreloprisan.brokerAPI.util.BrokerUtils;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatCallback;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatPayLoad;
import com.tudoreloprisan.tradingAPI.streaming.HeartBeatStreamingService;
import com.tudoreloprisan.tradingAPI.util.TradingUtils;

public abstract class BrokerStreamingService implements HeartBeatStreamingService {
	protected static final Logger LOG = Logger.getLogger(BrokerStreamingService.class);
	protected volatile boolean serviceUp = true;
	private final HeartBeatCallback<DateTime> heartBeatCallback;
	private final String hearbeatSourceId;
	protected Thread streamThread;
	private final BasicHeader authHeader;

	protected abstract String getStreamingUrl();

	protected abstract void startStreaming();

	protected abstract void stopStreaming();

	protected CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	protected BrokerStreamingService(String accessToken, HeartBeatCallback<DateTime> heartBeatCallback,
									 String heartbeatSourceId) {
		this.hearbeatSourceId = heartbeatSourceId;
		this.heartBeatCallback = heartBeatCallback;
		this.authHeader = BrokerUtils.createAuthHeader(accessToken);
	}

	protected void handleHeartBeat(JsonObject streamEvent) {
		String dateTimeAsString = streamEvent.get(BrokerJsonKeys.TIME.value()).getAsString();
		int lastDot = dateTimeAsString.lastIndexOf('.');
		dateTimeAsString = dateTimeAsString.substring(0, lastDot);
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
		DateTime heartBeatTime = DateTime.parse(dateTimeAsString, formatter);
		// = DateTime.parse(streamEvent.get(BrokerJsonKeys.TIME.value()).getAsString());
		heartBeatCallback.onHeartBeat(new HeartBeatPayLoad<DateTime>(heartBeatTime, hearbeatSourceId));
	}

	protected BufferedReader setUpStreamIfPossible(CloseableHttpClient httpClient) throws Exception {
		HttpUriRequest httpGet = new HttpGet(getStreamingUrl());
		httpGet.setHeader(authHeader);
		httpGet.setHeader(BrokerConstants.UNIX_DATETIME_HEADER);
		LOG.info(TradingUtils.executingRequestMsg(httpGet));
		HttpResponse resp = httpClient.execute(httpGet);
		HttpEntity entity = resp.getEntity();
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK && entity != null) {
			InputStream stream = entity.getContent();
			serviceUp = true;
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			return br;
		} else {
			String responseString = EntityUtils.toString(entity, "UTF-8");
			LOG.warn(responseString);
			return null;
		}
	}

	protected void handleDisconnect(String line) {
		serviceUp = false;
		LOG.warn(String.format("Disconnect message received for stream %s. PayLoad->%s", getHeartBeatSourceId(), line));
	}

	protected boolean isStreaming() {
		return serviceUp;
	}

	@Override
	public void stopHeartBeatStreaming() {
		stopStreaming();
	}

	@Override
	public void startHeartBeatStreaming() {
		startStreaming();
	}

	@Override
	public String getHeartBeatSourceId() {
		return this.hearbeatSourceId;
	}
}
