package brokerAPI.market;

import java.util.Collection;
import java.util.Map;

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

import com.google.common.collect.Maps;

import brokerAPI.account.BrokerConstants;
import brokerAPI.account.BrokerJsonKeys;
import brokerAPI.util.BrokerUtils;
import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.market.CurrentPriceInfoProvider;
import tradingAPI.market.Price;
import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;

public class BrokerCurrentPriceInfoProvider implements CurrentPriceInfoProvider<String, String> {

	private static final Logger LOG = Logger.getLogger(BrokerCurrentPriceInfoProvider.class);

	private final String url;
	private final BasicHeader authHeader;

	public BrokerCurrentPriceInfoProvider(String url, String accessToken) {
		this.url = url;
		this.authHeader = BrokerUtils.createAuthHeader(accessToken);
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	@Override
	public Map<TradeableInstrument<String>, Price<String>> getCurrentPricesForInstruments(
			Collection<TradeableInstrument<String>> instruments, String accountID) {
		StringBuilder instrumentCsv = new StringBuilder();
		boolean firstTime = true;
		for (TradeableInstrument<String> instrument : instruments) {
			if (firstTime) {
				firstTime = false;
			} else {
				instrumentCsv.append(TradingConstants.ENCODED_COMMA);
			}
			instrumentCsv.append(instrument.getInstrument());
		}

		Map<TradeableInstrument<String>, Price<String>> pricesMap = Maps.newHashMap();
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(this.url +BrokerConstants.ACCOUNTS_RESOURCE
					+ TradingConstants.FWD_SLASH+accountID+BrokerConstants.PRICES
					+ "?instruments=" + instrumentCsv.toString());
			httpGet.setHeader(this.authHeader);
			httpGet.setHeader(BrokerConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				JSONArray prices = (JSONArray) jsonResp.get(BrokerJsonKeys.PRICES.value());
				for (Object price : prices) {
					JSONObject trade = (JSONObject) price;
					DateTime priceTime = DateTime.parse((String) trade.get(BrokerJsonKeys.TIME.value()));
					TradeableInstrument<String> instrument = new TradeableInstrument<String>((String) trade
							.get(BrokerJsonKeys.INSTRUMENT.value()));
					JSONArray asks = (JSONArray) trade.get(BrokerJsonKeys.ASKS.value());
					
					double askPrice = 0;
					for (Object askObject : asks) {
						LOG.info(("ASK OBject:  ")+askObject.toString());
						if((long)((JSONObject)askObject).get(BrokerJsonKeys.LIQUIDITY.value())>0) {
							askPrice = Double.valueOf((String) ((JSONObject)askObject).get(BrokerJsonKeys.PRICE.value()));
							break;
						}
					}
					
					JSONArray bids = (JSONArray) trade.get(BrokerJsonKeys.BIDS.value());
					double bidPrice = 0;
					for (Object bidObject : bids) {
						if((long)((JSONObject)bidObject).get(BrokerJsonKeys.LIQUIDITY.value())>0) {
							bidPrice = Double.valueOf((String) ((JSONObject)bidObject).get(BrokerJsonKeys.PRICE.value()));
							break;
						}
					}
					
					Price<String> pi = new Price<String>(instrument, bidPrice, askPrice, priceTime);
					pricesMap.put(instrument, pi);
				}
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception ex) {
			LOG.error(ex);
			ex.printStackTrace();
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return pricesMap;
	}

}
