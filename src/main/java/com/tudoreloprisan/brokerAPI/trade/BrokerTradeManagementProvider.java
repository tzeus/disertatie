package com.tudoreloprisan.brokerAPI.trade;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tudoreloprisan.brokerAPI.account.BrokerConstants;
import com.tudoreloprisan.brokerAPI.account.BrokerJsonKeys;
import com.tudoreloprisan.brokerAPI.util.BrokerUtils;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.trade.Trade;
import com.tudoreloprisan.tradingAPI.trade.TradeManagementProvider;
import com.tudoreloprisan.tradingAPI.trade.TradingSignal;
import com.tudoreloprisan.tradingAPI.util.TradingConstants;
import com.tudoreloprisan.tradingAPI.util.TradingUtils;
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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collection;

public class BrokerTradeManagementProvider implements TradeManagementProvider<String, String, String> {

    private static final Logger LOG = Logger.getLogger(BrokerTradeManagementProvider.class);

    private final String url;
    private final BasicHeader authHeader;

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

    String getOpenTradesInfoUrl(String accountId) {
        return this.url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId
                + BrokerConstants.OPEN_TRADES_RESOURCE;
    }

    private Trade<String, String, String> parseTrade(JsonObject trade, String accountId) {
        JsonObject tradeJsonObject = trade;
        if (tradeJsonObject.keySet().contains("trade")) {
            tradeJsonObject = trade.getAsJsonObject(BrokerJsonKeys.TRADE.value());
        }
        String dateTimeAsString = tradeJsonObject.get(BrokerJsonKeys.OPEN_TIME.value()).getAsString();
        int lastDot = dateTimeAsString.lastIndexOf('.');
        dateTimeAsString = dateTimeAsString.substring(0, lastDot);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");
        DateTime dateTime = formatter.parseDateTime(dateTimeAsString);
        final String tradeId = tradeJsonObject.get(BrokerJsonKeys.ID.value()).getAsString();
        final String tradeUnits = tradeJsonObject.get(BrokerJsonKeys.CURRENT_UNITS.value()).getAsString();
        final TradingSignal tradeSignal = tradeUnits.startsWith("-") ? TradingSignal.SHORT : TradingSignal.LONG;
        final TradeableInstrument<String> tradeInstrument = new TradeableInstrument<>(
                tradeJsonObject.get(BrokerJsonKeys.INSTRUMENT.value()).getAsString());

        JsonObject takeProfitObject = tradeJsonObject.getAsJsonObject(BrokerJsonKeys.TAKE_PROFIT_ORDER.value());
        final double tradeTakeProfit = takeProfitObject == null ? 0 : takeProfitObject.get(BrokerJsonKeys.PRICE.value()).getAsDouble();

        final double tradeExecutionPrice = tradeJsonObject.get(BrokerJsonKeys.PRICE.value()).getAsDouble();

        JsonObject stopLossObject = (JsonObject) tradeJsonObject.get(BrokerJsonKeys.STOP_LOSS_ORDER.value());
        final double tradeStopLoss = stopLossObject == null ? 0 : stopLossObject.get(BrokerJsonKeys.PRICE.value()).getAsDouble();
        final String state = tradeJsonObject.get(BrokerJsonKeys.ORDER_STATE.value()).getAsString();
        final String realizedPL = tradeJsonObject.get(BrokerJsonKeys.REALIZED_PL.value()).getAsString();
        final String unRealizedPL = tradeJsonObject.get(BrokerJsonKeys.UNREALIZED_PL.value()).getAsString();
        final String financing = tradeJsonObject.get(BrokerJsonKeys.FINANCING.value()).getAsString();
        final String initialMarginRequired = tradeJsonObject.get(BrokerJsonKeys.INITIAL_MARGIN_REQUIRED.value()).getAsString();
        final String marginUsed = tradeJsonObject.get(BrokerJsonKeys.MARGIN_USED.value()).getAsString();


        return new Trade<String, String, String>(tradeId, tradeUnits, tradeSignal, tradeInstrument, dateTime,
                tradeTakeProfit, tradeExecutionPrice, tradeStopLoss, accountId, state, realizedPL, unRealizedPL, financing, initialMarginRequired, marginUsed);

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
                JsonObject jsonResp = new GsonBuilder().disableHtmlEscaping().create().fromJson(strResp, JsonObject.class);
                JsonArray accountTrades = jsonResp.getAsJsonArray(BrokerJsonKeys.TRADES.value());
                for (Object accountTrade : accountTrades) {
                    JsonObject trade = (JsonObject) accountTrade;
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

    @Override
    public Collection<Trade<String, String, String>> getOpenTradesForAccount(String accountId) {
        Collection<Trade<String, String, String>> allTrades = Lists.newArrayList();
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpUriRequest httpGet = new HttpGet(getOpenTradesInfoUrl(accountId));
            httpGet.setHeader(this.authHeader);
            httpGet.setHeader(BrokerConstants.UNIX_DATETIME_HEADER);
            LOG.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse resp = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(resp);
            if (strResp != StringUtils.EMPTY) {
                JsonObject jsonResp = new GsonBuilder().disableHtmlEscaping().create().fromJson(strResp, JsonObject.class);
                JsonArray accountTrades = jsonResp.getAsJsonArray(BrokerJsonKeys.TRADES.value());
                for (Object accountTrade : accountTrades) {
                    JsonObject trade = (JsonObject) accountTrade;
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
    public <T> T getTradeForAccount(String tradeId, String accountId) {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpUriRequest httpGet = new HttpGet(getTradeForAccountUrl(tradeId, accountId));
            httpGet.setHeader(this.authHeader);
            httpGet.setHeader(BrokerConstants.UNIX_DATETIME_HEADER);
            LOG.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse resp = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(resp);
            if (strResp != StringUtils.EMPTY) {
                JsonObject trade = new GsonBuilder().disableHtmlEscaping().create().fromJson(strResp, JsonObject.class);
                return (T) parseTrade(trade, accountId);
            } else {
                return (T) TradingUtils.printErrorMsg(resp);
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
        JsonObject jsonObject = new JsonObject();

        JsonObject takeProfitJSON = new JsonObject();
        takeProfitJSON.add(BrokerJsonKeys.PRICE.value(), new Gson().toJsonTree(takeProfit));

        JsonObject stopLossJSON = new JsonObject();
        stopLossJSON.add(BrokerJsonKeys.PRICE.value(), new Gson().toJsonTree(stopLoss));

        jsonObject.add(BrokerJsonKeys.TAKE_PROFIT.value(), takeProfitJSON);
        jsonObject.add(BrokerJsonKeys.STOP_LOSS.value(), stopLossJSON);
        StringEntity stringEntity = new StringEntity(jsonObject.getAsString());
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
            HttpPut httpPut = new HttpPut(getTradeForAccountUrl(tradeId, accountId) +
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
