package brokerAPI.trade;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.Lists;

import brokerAPI.account.BrokerConstants;
import brokerAPI.account.BrokerJsonKeys;
import brokerAPI.util.BrokerUtils;
import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.trade.Trade;
import tradingAPI.trade.TradeManagementProvider;
import tradingAPI.trade.TradingSignal;
import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;

public class BrokerTradeManagementProvider implements TradeManagementProvider<String, String, String> {

	private static final Logger	LOG	= Logger.getLogger(BrokerTradeManagementProvider.class);

	private final String		url;
	private final BasicHeader	authHeader;

	public BrokerTradeManagementProvider(String url, String accessToken) {
		this.url = url;
		this.authHeader = BrokerUtils.createAuthHeader(accessToken);
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	String getTradesInfoUrl(String accountId) {
		return this.url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
				+ BrokerConstants.TRADES_RESOURCE;
	}

	private Trade<String, String, String> parseTrade(JSONObject trade, String accountId) {
		DateTime tradeTime = DateTime.parse((String) trade.get(BrokerJsonKeys.OPEN_TIME.value()));
		final String tradeId = (String) trade.get(BrokerJsonKeys.ID.value());
		final String tradeUnits = (String) trade.get(BrokerJsonKeys.CURRENT_UNITS.value());
		final TradingSignal tradeSignal = tradeUnits.startsWith("-") ? TradingSignal.SHORT : TradingSignal.LONG;
		final TradeableInstrument<String> tradeInstrument = new TradeableInstrument<String>(
				(String) trade.get(BrokerJsonKeys.INSTRUMENT.value()));

		JSONObject takeProfitObject = (JSONObject) trade.get(BrokerJsonKeys.TAKE_PROFIT_ORDER.value());
		final double tradeTakeProfit = takeProfitObject==null?0:Double.valueOf((String) takeProfitObject.get(BrokerJsonKeys.PRICE.value()));

		final double tradeExecutionPrice = Double.valueOf((String) trade.get(BrokerJsonKeys.PRICE.value()));

		JSONObject stopLossObject = (JSONObject) trade.get(BrokerJsonKeys.STOP_LOSS_ORDER.value());
		final double tradeStopLoss = stopLossObject==null?0:Double.valueOf((String) stopLossObject.get(BrokerJsonKeys.PRICE.value()));

		return new Trade<String, String, String>(tradeId, tradeUnits, tradeSignal, tradeInstrument, tradeTime,
				tradeTakeProfit, tradeExecutionPrice, tradeStopLoss, accountId);

	}

	@Override
	public Collection<Trade<String, String, String>> getTradesForAccount(String accountId) {
		Collection<Trade<String, String, String>> allTrades = Lists.newArrayList();
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(getTradesInfoUrl(accountId));
			httpGet.setHeader(this.authHeader);
			httpGet.setHeader(BrokerConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				JSONArray accountTrades = (JSONArray) jsonResp.get(BrokerJsonKeys.TRADES.value());
				for (Object accountTrade : accountTrades) {
					JSONObject trade = (JSONObject) accountTrade;
					Trade<String, String, String> tradeInfo = parseTrade(trade, accountId);
					allTrades.add(tradeInfo);
				}
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception ex) {
			LOG.error("error encountered whilst fetching trades for account:" + accountId, ex);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return allTrades;
	}

	String getTradeForAccountUrl(String tradeId, String accountId) {
		return this.url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
				+ BrokerConstants.TRADES_RESOURCE + TradingConstants.FWD_SLASH + tradeId;
	}

	@Override
	public Trade<String, String, String> getTradeForAccount(String tradeId, String accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(getTradeForAccountUrl(tradeId, accountId));
			httpGet.setHeader(this.authHeader);
			httpGet.setHeader(BrokerConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				JSONObject trade = (JSONObject) JSONValue.parse(strResp);
				return parseTrade(trade, accountId);
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception ex) {
			LOG.error(String.format("error encountered whilst fetching trade %d for account %d", tradeId, accountId),
					ex);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return null;
	}

	private HttpPut createPutCommand(String accountId, String tradeId, double stopLoss, double takeProfit)
			throws Exception {
		HttpPut httpPut = new HttpPut(getTradeForAccountUrl(tradeId, accountId) + BrokerConstants.ORDERS_RESOURCE);
		httpPut.setHeader(this.authHeader);
		JSONObject jsonObject = new JSONObject();

		JSONObject takeProfitJSON = new JSONObject();
		takeProfitJSON.put(BrokerJsonKeys.PRICE.value(), String.valueOf(takeProfit));

		JSONObject stopLossJSON = new JSONObject();
		stopLossJSON.put(BrokerJsonKeys.PRICE.value(), String.valueOf(stopLoss));

		jsonObject.put(BrokerJsonKeys.TAKE_PROFIT.value(), takeProfitJSON);
		jsonObject.put(BrokerJsonKeys.STOP_LOSS.value(), stopLossJSON);
		StringEntity stringEntity = new StringEntity(jsonObject.toJSONString());
		httpPut.setEntity(stringEntity);
		return httpPut;
	}

	@Override
	public boolean modifyTrade(String accountId, String tradeId, double stopLoss, double takeProfit) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpPut httpPut = createPutCommand(accountId, tradeId, stopLoss, takeProfit);
			LOG.info(TradingUtils.executingRequestMsg(httpPut));
			HttpResponse resp = httpClient.execute(httpPut);
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if (resp.getEntity() != null) {
					LOG.info("Trade Modified->" + TradingUtils.responseToString(resp));
				} else {
					LOG.warn(String.format("trade %d could not be modified with stop loss %3.5f", tradeId, stopLoss));
				}

				return true;
			} else {
				LOG.warn(String.format(
						"trade %d could not be modified with stop loss %3.5f and take profit %3.5f. http code=%d",
						tradeId, stopLoss, takeProfit, resp.getStatusLine().getStatusCode()));
			}
		} catch (Exception e) {
			LOG.error(
					String.format("error while modifying trade %d to stop loss %3.5f, take profit %3.5f for account %d",
							tradeId, stopLoss, takeProfit, accountId),
					e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return false;
	}

	@Override
	public boolean closeTrade(String tradeId, String accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpPut httpPut = new HttpPut(getTradeForAccountUrl(tradeId, accountId)+
					BrokerConstants.TRADES_CLOSE_RESOURCE);
			httpPut.setHeader(authHeader);
			LOG.info(TradingUtils.executingRequestMsg(httpPut));
			HttpResponse resp = httpClient.execute(httpPut);
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				LOG.info(String.format("Trade %d successfully closed for account %d", tradeId, accountId));
				return true;
			} else {
				LOG.warn(String.format("Trade %d could not be closed. Recd error code %d", tradeId,
						resp.getStatusLine().getStatusCode()));
			}
		} catch (Exception e) {
			LOG.warn("error deleting trade id:" + tradeId, e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return false;
	}

}
