package oandaAPI.instruments;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.Lists;

import oandaAPI.account.OandaConstants;
import oandaAPI.account.OandaJsonKeys;
import oandaAPI.util.OandaUtils;
import tradingAPI.instruments.InstrumentDataProvider;
import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;

public class OandaInstrumentDataProviderService implements InstrumentDataProvider<String> {

	private static final Logger	LOG				= Logger.getLogger(OandaInstrumentDataProviderService.class);

	private final String		url;
	private final String		accountId;
	private final BasicHeader	authHeader;
	

	public OandaInstrumentDataProviderService(String url, String accountId, String accessToken) {
		this.url = url; // OANDA REST service base url
		this.accountId = accountId;// OANDA valid account id
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	String getInstrumentsUrl() {
		return url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + 
				OandaConstants.INSTRUMENTS_RESOURCE;
	}

	@Override
	public Collection<TradeableInstrument<String>> getInstruments() {
		Collection<TradeableInstrument<String>> instrumentsList = Lists.newArrayList();
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(getInstrumentsUrl());
			httpGet.setHeader(authHeader);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				LOG.info(strResp);
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				
				JSONArray instrumentArray = (JSONArray) jsonResp.get(OandaJsonKeys.INSTRUMENTS.value());
				LOG.info(instrumentArray==null);
				for (Object o : instrumentArray) {
					JSONObject instrumentJson = (JSONObject) o;
					String instrument = (String) instrumentJson.get(OandaJsonKeys.NAME.value());
					String[] currencies = OandaUtils.splitCcyPair(instrument);
					Double pip = Math.pow(10, Double.parseDouble(instrumentJson.get(OandaJsonKeys.PIP.value()).toString()));
										
					TradeableInstrument<String> tradeableInstrument = new TradeableInstrument<String>(instrument, pip,
							null);
					instrumentsList.add(tradeableInstrument);
				}
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception e) {
			LOG.error("exception encountered whilst retrieving all instruments info", e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return instrumentsList;
	}

}
