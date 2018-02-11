package oandaAPI.account;

import java.util.Collection;
import java.util.List;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.Lists;


import oandaAPI.account.OandaJsonKeys;
import oandaAPI.util.OandaUtils;
import tradingAPI.account.Account;
import tradingAPI.account.AccountDataProvider;
import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;

public class OandaAccountDataProviderService implements AccountDataProvider<String> {
	private static final Logger LOG = Logger.getLogger(OandaAccountDataProviderService.class);

	private final String url;
	private final String userName;
	private final BasicHeader authHeader;

	public OandaAccountDataProviderService(final String url, final String userName, final String accessToken) {
		this.url = url;
		this.userName = userName;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
	}

	CloseableHttpClient getHttpClient() {
		return HttpClientBuilder.create().build();
	}

	String getSingleAccountUrl(String accountId) {
		return url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId;
	}

	String getAllAccountsUrl() {
		return this.url + OandaConstants.ACCOUNTS_RESOURCE;
	}

	private Account<String> getLatestAccountInfo(final String accountId, CloseableHttpClient httpClient) {
		try {
			HttpUriRequest httpGet = new HttpGet(getSingleAccountUrl(accountId));
			httpGet.setHeader(authHeader);

			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse httpResponse = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(httpResponse);
			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				
				JSONObject wrapper = (JSONObject) obj;
				JSONObject accountJson = (JSONObject) wrapper.get(OandaJsonKeys.ACCOUNT.value());
				System.out.println(accountJson.toJSONString());
				/*Parse JSON response for account information*/
				final double accountBalance = (Double.valueOf((String) accountJson.get(OandaJsonKeys.BALANCE.value()))).doubleValue();
				final double accountUnrealizedPnl = (Double.valueOf((String) accountJson.get(OandaJsonKeys.UNREALIZED_PL.value()))).doubleValue();
				String realizedPL = (String) accountJson.get(OandaJsonKeys.REALIZED_PL.value());
				final double accountRealizedPnl = (realizedPL!=null?Double.valueOf(realizedPL).doubleValue():0);
				final double accountMarginUsed = (Double.valueOf((String) accountJson.get(OandaJsonKeys.MARGIN_USED.value()))).doubleValue();
				final double accountMarginAvailable = (Double.valueOf((String) accountJson.get(OandaJsonKeys.MARGIN_AVAIL.value()))).doubleValue();
				final Long accountOpenTrades = (Long) accountJson.get(OandaJsonKeys.OPEN_TRADES.value());
				final String accountBaseCurrency = (String) accountJson.get(OandaJsonKeys.ACCOUNT_CURRENCY.value());
				final Double accountLeverage = Double.valueOf((String) accountJson.get(OandaJsonKeys.MARGIN_RATE.value()));

				Account<String> accountInfo = new Account<String>(accountBalance, accountUnrealizedPnl, accountRealizedPnl,
						accountMarginUsed, accountMarginAvailable, accountOpenTrades, accountBaseCurrency, accountId,
						accountLeverage);

				return accountInfo;
			} else {
				TradingUtils.printErrorMsg(httpResponse);
			}
		} catch (Exception e) {
			LOG.error("Exception encountered whilst getting info for account:" + accountId, e);
		}
		return null;
	}
	
	@Override
	public Account<String> getLatestAccountInfo(String accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			return getLatestAccountInfo(accountId, httpClient);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
	}
	
	@Override
	public Collection<Account<String>> getLatestAccountsInfo() {
		
		LOG.log(Level.INFO, "Entering method getLatestAccountsInfo in OADPS");
		CloseableHttpClient httpClient = getHttpClient();
		List<Account<String>> accInfos = Lists.newArrayList();
		try {
			
			HttpUriRequest httpGet = new HttpGet(getAllAccountsUrl());
			httpGet.setHeader(this.authHeader);

			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			LOG.log(Level.INFO, strResp);
			if (strResp != StringUtils.EMPTY) {
				JSONObject jsonResp = (JSONObject) JSONValue.parse(strResp);
				
				JSONArray accounts = (JSONArray) jsonResp.get(OandaJsonKeys.ACCOUNTS.value());
				
				/*
				 * We are doing a per account json request because not all information is returned in the array of results
				 */
				for (Object o : accounts) {
					JSONObject account = (JSONObject) o;
					String accountIdentifier = (String) account.get(OandaJsonKeys.ACCOUNT_ID.value());
					LOG.info("Got ID "+accountIdentifier);
					Account<String> accountInfo = getLatestAccountInfo(accountIdentifier, httpClient);
					LOG.info("ID: "+accountInfo.getAccountId()+" Currency: "+accountInfo.getCurrency());
					accInfos.add(accountInfo);
				}
			} else {
				TradingUtils.printErrorMsg(resp);
			}

		} catch (Exception e) {
			LOG.error("Exception encountered while retrieving all accounts data", e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return accInfos;
	}

}
