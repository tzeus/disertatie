/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package brokerAPI.account;

import java.util.Collection;
import java.util.List;

import brokerAPI.util.BrokerUtils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tradingAPI.account.Account;
import tradingAPI.account.AccountDataProvider;

import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;


public class BrokerAccountDataProviderService implements AccountDataProvider<String> {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(BrokerAccountDataProviderService.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Instance fields 
    //~ ----------------------------------------------------------------------------------------------------------------

    private final String url;
    private final String userName;
    private final BasicHeader authHeader;

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Constructors 
    //~ ----------------------------------------------------------------------------------------------------------------

    public BrokerAccountDataProviderService(final String url, final String userName, final String accessToken) {
        this.url = url;
        this.userName = userName;
        this.authHeader = BrokerUtils.createAuthHeader(accessToken);
    }

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

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
                JsonObject jsonResp = new Gson().fromJson(strResp, JsonObject.class);

                JsonArray accounts = jsonResp.get(BrokerJsonKeys.ACCOUNTS.value()).getAsJsonArray();

                /*
                 * We are doing a per account json request because not all information is returned in the array of
                 * results
                 */
                for (Object acc : accounts) {
                    JsonObject account = (JsonObject) acc;
                    String accountIdentifier = account.get(BrokerJsonKeys.ACCOUNT_ID.value()).getAsString();
                    LOG.info("Got ID " + accountIdentifier);
                    Account<String> accountInfo = getLatestAccountInfo(accountIdentifier, httpClient);
                    LOG.info("ID: " + accountInfo.getAccountId() + " Currency: " + accountInfo.getCurrency());
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

    CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }

    String getSingleAccountUrl(String accountId) {
        return url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId;
    }

    String getAllAccountsUrl() {
        return this.url + BrokerConstants.ACCOUNTS_RESOURCE;
    }

    private Account<String> getLatestAccountInfo(final String accountId, CloseableHttpClient httpClient) {
        try {
            HttpUriRequest httpGet = new HttpGet(getSingleAccountUrl(accountId));
            httpGet.setHeader(authHeader);

            LOG.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(httpResponse);
            System.out.println(strResp);
            if (strResp != StringUtils.EMPTY) {
                JsonObject wrapper = new Gson().fromJson(strResp, JsonObject.class);

                JsonObject accountJson = (JsonObject) wrapper.get(BrokerJsonKeys.ACCOUNT.value());
                System.out.println(accountJson);
                /*Parse JSON response for account information*/
                final double accountBalance = accountJson.get(BrokerJsonKeys.BALANCE.value()).getAsDouble();
                final double accountUnrealizedPnl = accountJson.get(BrokerJsonKeys.UNREALIZED_PL.value()).getAsDouble();
                JsonElement jsonElement = accountJson.get(BrokerJsonKeys.REALIZED_PL.value());
                final double accountRealizedPnl = ((jsonElement != null) ? jsonElement.getAsDouble() : 0);
                final double accountMarginUsed = accountJson.get(BrokerJsonKeys.MARGIN_USED.value()).getAsDouble();
                final double accountMarginAvailable = accountJson.get(BrokerJsonKeys.MARGIN_AVAIL.value()).getAsDouble();
                final Long accountOpenTrades = accountJson.get(BrokerJsonKeys.OPEN_TRADES.value()).getAsLong();
                final String accountBaseCurrency = accountJson.get(BrokerJsonKeys.ACCOUNT_CURRENCY.value()).getAsString();
                final Double accountLeverage = accountJson.get(BrokerJsonKeys.MARGIN_RATE.value()).getAsDouble();

                Account<String> accountInfo = new Account<String>(accountBalance, accountUnrealizedPnl, accountRealizedPnl, accountMarginUsed, accountMarginAvailable, accountOpenTrades, accountBaseCurrency, accountId,
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

}
