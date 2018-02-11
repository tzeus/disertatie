package oandaAPI.streaming;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import org.json.simple.JSONObject;

import oandaAPI.account.OandaConstants;
import oandaAPI.account.OandaJsonKeys;
import oandaAPI.util.OandaUtils;
import tradingAPI.heartbeat.HeartBeatCallback;
import tradingAPI.util.TradingUtils;

public abstract class OandaStreamingService implements HeartBeatStreamingService {
	protected static final Logger LOG = Logger.getLogger(OandaStreamingService.class);
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

	protected OandaStreamingService(String accessToken, HeartBeatCallback<DateTime> heartBeatCallback,
			String heartbeatSourceId) {
		this.hearbeatSourceId = heartbeatSourceId;
		this.heartBeatCallback = heartBeatCallback;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	protected void handleHeartBeat(JSONObject streamEvent) {		
		DateTime heartBeatTime = DateTime.parse( (String) streamEvent.get(OandaJsonKeys.TIME.value()));
		heartBeatCallback.onHeartBeat(new HeartBeatPayLoad<DateTime>(heartBeatTime, hearbeatSourceId));
	}

	protected BufferedReader setUpStreamIfPossible(CloseableHttpClient httpClient) throws Exception {
		HttpUriRequest httpGet = new HttpGet(getStreamingUrl());
		httpGet.setHeader(authHeader);
		httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);
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
