package com.tudoreloprisan.brokerAPI.order;

import java.util.Collection;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import com.tudoreloprisan.brokerAPI.account.BrokerConstants;
import com.tudoreloprisan.brokerAPI.account.BrokerJsonKeys;
import com.tudoreloprisan.brokerAPI.util.BrokerUtils;
import com.tudoreloprisan.tradingAPI.account.Account;
import com.tudoreloprisan.tradingAPI.account.AccountDataProvider;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.order.Order;
import com.tudoreloprisan.tradingAPI.order.OrderManagementProvider;
import com.tudoreloprisan.tradingAPI.order.OrderType;
import com.tudoreloprisan.tradingAPI.trade.TradingSignal;
import com.tudoreloprisan.tradingAPI.util.TradingConstants;
import com.tudoreloprisan.tradingAPI.util.TradingUtils;

public class BrokerOrderManagementProvider implements OrderManagementProvider<String, String, String> {

    private static final Logger LOG = Logger.getLogger(BrokerOrderManagementProvider.class);

    private final String url;
    private final BasicHeader authHeader;
    private final AccountDataProvider<String> accountDataProvider;

    public BrokerOrderManagementProvider(String url, String accessToken, AccountDataProvider<String> accountDataProvider) {
        this.url = url;
        this.authHeader = BrokerUtils.createAuthHeader(accessToken);
        this.accountDataProvider = accountDataProvider;
    }

    CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create().build();
    }

    public String getAllTransactions(String accountId, String sinceId) {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpGet httpGet = new HttpGet(transactionsSinceIdUrl(accountId, sinceId));
            httpGet.setHeader(authHeader);
            LOG.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse resp = httpClient.execute(httpGet);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                LOG.info(String.format("Successfully fetched transactions for account %s", accountId));
                return TradingUtils.responseToString(resp);
            } else {
                LOG.warn(String.format("Could not fetch. Recd error code %d", resp
                        .getStatusLine().getStatusCode()));
            }
        } catch (Exception e) {
            LOG.warn("error fetching transactions:", e);
        } finally {
            TradingUtils.closeSilently(httpClient);
        }
        return "Error";

    }

    @Override
    public boolean closeOrder(String orderId, String accountId) {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpPut httpPut = new HttpPut(cancelOrderForAccountUrl(accountId, orderId));
            httpPut.setHeader(authHeader);
            LOG.info(TradingUtils.executingRequestMsg(httpPut));
            HttpResponse resp = httpClient.execute(httpPut);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                LOG.info(String.format("Order %d successfully deleted for account %d", orderId, accountId));
                return true;
            } else {
                LOG.warn(String.format("Order %d could not be deleted. Recd error code %d", orderId, resp
                        .getStatusLine().getStatusCode()));
            }
        } catch (Exception e) {
            LOG.warn("error deleting order id:" + orderId, e);
        } finally {
            TradingUtils.closeSilently(httpClient);
        }
        return false;
    }

    HttpPut createPutCommand(Order<String, String> order, String accountId) throws Exception {
        HttpPut httpPut = new HttpPut(orderForAccountUrl(accountId, order.getOrderId()));
        httpPut.setHeader(this.authHeader);
        httpPut.setHeader("content-type", "application/json");
        JsonObject jsonObject = getJSONForNewOrder(order);
        StringEntity jsonStringEntity = new StringEntity(jsonObject.getAsString());
        httpPut.setEntity(jsonStringEntity);
        return httpPut;
    }

    HttpPost createPostCommand(Order<String, String> order, String accountId) throws Exception {
        HttpPost httpPost = new HttpPost(this.url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH
                + accountId + BrokerConstants.ORDERS_RESOURCE);
        httpPost.setHeader(this.authHeader);
        httpPost.setHeader("content-type", "application/json");
        JsonObject jsonObject = getJSONForNewOrder(order);
        StringEntity jsonStringEntity = new StringEntity(new Gson().toJson(jsonObject));
        httpPost.setEntity(jsonStringEntity);
        return httpPost;
    }

    @Override
    public String placeOrder(Order<String, String> order, String accountId) {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpPost httpPost = createPostCommand(order, accountId);
            LOG.info(TradingUtils.executingRequestMsg(httpPost));
            HttpResponse resp = httpClient.execute(httpPost);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                if (resp.getEntity() != null) {
                    String strResp = TradingUtils.responseToString(resp);
                    JsonObject responseJsonObject = new GsonBuilder().disableHtmlEscaping().create().fromJson(strResp, JsonObject.class);
                    JsonObject orderResponse = responseJsonObject.getAsJsonObject(BrokerJsonKeys.
                            ORDER_CREATE_TRANSACTION.value());
                    String orderId = orderResponse.get(BrokerJsonKeys.ID.value()).getAsString();
                    LOG.info("Order executed->" + strResp);
                    return new GsonBuilder().disableHtmlEscaping().create().toJson(orderResponse);
                } else {
                    return null;
                }

            } else {
                LOG.info(String.format("Order not executed. http code=%d. Order pojo->%s",
                        resp.getStatusLine().getStatusCode(), order.toString()));
                return null;
            }
        } catch (Exception e) {
            LOG.warn(e);
            return null;
        } finally {
            TradingUtils.closeSilently(httpClient);
        }
    }

    @Override
    public Order pendingOrderForAccount(String orderId, String accountId) {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpUriRequest httpGet = new HttpGet(orderForAccountUrl(accountId, orderId));
            httpGet.setHeader(this.authHeader);
            httpGet.setHeader(BrokerConstants.UNIX_DATETIME_HEADER);
            LOG.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse resp = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(resp);
            if (strResp != StringUtils.EMPTY) {
                JsonObject order = new GsonBuilder().disableHtmlEscaping().create().fromJson(strResp, JsonObject.class);
                return parseOrder(order);
            } else {
                TradingUtils.printErrorMsg(resp);
            }
        } catch (Exception e) {
            LOG.error(String.format("error encountered whilst fetching pending order for account %d and order id %d",
                    accountId, orderId), e);
        } finally {
            TradingUtils.closeSilently(httpClient);
        }
        return null;
    }

    private Order parseOrder(JsonObject order) {
        JsonObject orderAsJson = order;
        if (order.keySet().contains("order")) {
            orderAsJson = order.getAsJsonObject(BrokerJsonKeys.ORDER.value());
        }
        LOG.info(String.format("Parsing order... %s", order.toString()));
        JsonElement orderTypeAsJson = orderAsJson.get(BrokerJsonKeys.TYPE.value());
        final OrderType orderType = BrokerUtils.toOrderType(orderTypeAsJson.getAsString());
        if (orderType == OrderType.STOP_LOSS || orderType == OrderType.TAKE_PROFIT) {
//			return (T) orderAsJson.get(BrokerJsonKeys.TRADE_ID_CAPS.value()).getAsString(); //THESE ORDERS COME BACK ON TRADE ENDPOINT
            return null;
        }
        final String orderInstrument = orderAsJson.get(BrokerJsonKeys.INSTRUMENT.value()).getAsString();

        final String orderUnits = orderAsJson.get(BrokerJsonKeys.UNITS.value()).getAsString();
        final TradingSignal orderSide = orderUnits.startsWith("-") ? TradingSignal.SHORT : TradingSignal.LONG;


        JsonObject takeProfitJson = (JsonObject) orderAsJson.get(BrokerJsonKeys.TAKE_PROFIT_ON_FILL.value());
        final double orderTakeProfit = takeProfitJson == null ? 0 : takeProfitJson.get(BrokerJsonKeys.PRICE.value()).getAsDouble();

        JsonObject stopLossJson = (JsonObject) orderAsJson.get(BrokerJsonKeys.STOP_LOSS_ON_FILL.value());
        final double orderStopLoss = stopLossJson == null ? 0 : stopLossJson.get(BrokerJsonKeys.PRICE.value()).getAsDouble();

        JsonElement price = orderAsJson.get(BrokerJsonKeys.PRICE.value());
        final double orderPrice = price == null ? 0 : price.getAsDouble();

        String orderId = orderAsJson.get(BrokerJsonKeys.ID.value()).getAsString();


        Order<String, String> pendingOrder = new Order<String, String>(new TradeableInstrument<String>(orderInstrument),
                orderUnits, orderSide, orderType, orderTakeProfit, orderStopLoss, orderPrice);
        pendingOrder.setOrderId(orderId);

        return pendingOrder;
    }

    @Override
    public Collection<Order<String, String>> pendingOrdersForInstrument(TradeableInstrument<String> instrument) {
        Collection<Account<String>> accounts = this.accountDataProvider.getLatestAccountsInfo();
        Collection<Order<String, String>> allOrders = Lists.newArrayList();
        for (Account<String> account : accounts) {
            allOrders.addAll(this.pendingOrdersForAccount(account.getAccountId(), instrument));
        }
        return allOrders;
    }

    @Override
    public Collection<Order<String, String>> allPendingOrders() {
        return pendingOrdersForInstrument(null);
    }

    private Collection<Order<String, String>> pendingOrdersForAccount(String accountId,
                                                                      TradeableInstrument<String> instrument) {
        Collection<Order<String, String>> pendingOrders = Lists.newArrayList();
        CloseableHttpClient httpClient = getHttpClient();
        try {

            HttpUriRequest httpGet = new HttpGet(this.url + BrokerConstants.ACCOUNTS_RESOURCE
                    + TradingConstants.FWD_SLASH + accountId +
                    (instrument == null ? (BrokerConstants.PENDING_ORDERS_RESOURCE) : (BrokerConstants.ORDERS_RESOURCE +
                            "?instrument=" + instrument.getInstrument())));


            httpGet.setHeader(this.authHeader);
            httpGet.setHeader(BrokerConstants.UNIX_DATETIME_HEADER);
            LOG.info(TradingUtils.executingRequestMsg(httpGet));
            HttpResponse resp = httpClient.execute(httpGet);
            String strResp = TradingUtils.responseToString(resp);
            if (strResp != StringUtils.EMPTY) {
                JsonObject jsonResp = new GsonBuilder().disableHtmlEscaping().create().fromJson(strResp, JsonObject.class);
                JsonArray accountOrders = jsonResp.get(BrokerJsonKeys.ORDERS.value()).getAsJsonArray();
                for (Object o : accountOrders) {
                    JsonObject order = (JsonObject) o;
                    Order<String, String> pendingOrder = parseOrder(order);
                    if (pendingOrder != null)
                        pendingOrders.add(pendingOrder);
                }
            } else {
                TradingUtils.printErrorMsg(resp);
            }
        } catch (Exception e) {
            LOG.error(String.format("error encountered whilst fetching pending orders for account %s and instrument %s",
                    accountId, instrument == null ? "NONE" : instrument.getInstrument()), e);
        } finally {
            TradingUtils.closeSilently(httpClient);
        }
        return pendingOrders;
    }

    @Override
    public Collection<Order<String, String>> pendingOrdersForAccount(String accountId) {
        return this.pendingOrdersForAccount(accountId, null);
    }

    public String orderForAccountUrl(String accountId, String orderId) {
        return this.url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId +
                BrokerConstants.ORDERS_RESOURCE + TradingConstants.FWD_SLASH + orderId;
    }

    String cancelOrderForAccountUrl(String accountId, String orderId) {
        return this.url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId +
                BrokerConstants.ORDERS_RESOURCE + TradingConstants.FWD_SLASH + orderId + BrokerConstants.CANCEL_RESOURCE;
    }

    public String transactionsSinceIdUrl(String accountId, String sinceId) {

        return this.url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId +
                BrokerConstants.TRANSACTIONS + TradingConstants.FWD_SLASH + "sinceid?id=" + (sinceId == null ? "1" : sinceId);

    }

    @Override
    public boolean modifyOrder(Order<String, String> order, String accountId) {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            HttpPut httpPut = createPutCommand(order, accountId);
            LOG.info(TradingUtils.executingRequestMsg(httpPut));
            HttpResponse resp = httpClient.execute(httpPut);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED && resp.getEntity() != null) {
                String strResp = TradingUtils.responseToString(resp);
                Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                JsonObject orderResponse = gson.fromJson(strResp, JsonObject.class);
                String orderId = orderResponse.get(BrokerJsonKeys.ID.value()).getAsString();
                order.setOrderId(orderId);
                LOG.info("Order Modified->" + TradingUtils.responseToString(resp));
                return true;
            }
            LOG.warn(String.format("order %s could not be modified.", order.toString()));
        } catch (Exception e) {
            LOG.error(String.format("error encountered whilst modifying order %d for account %d", order, accountId), e);
        } finally {
            TradingUtils.closeSilently(httpClient);
        }
        return false;
    }

    @SuppressWarnings("unchecked")// JsonObject contains strings as well as other JsonObjects
    private JsonObject getJSONForNewOrder(Order<String, String> order) {
        JsonObject jsonObject = new JsonObject();
        JsonObject jsonOrderBody = new JsonObject();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        jsonOrderBody.add(BrokerJsonKeys.INSTRUMENT.value(), gson.toJsonTree(order.getInstrument().getInstrument()));
        jsonOrderBody.add(BrokerJsonKeys.UNITS.value(), gson.toJsonTree(order.getUnits()));
        jsonOrderBody.add(BrokerJsonKeys.TYPE.value(), gson.toJsonTree(order.getType().toString()));

        if (order.getStopLoss() != 0.0) {
            JsonObject stopLossJsonObject = new JsonObject();
            stopLossJsonObject.add(BrokerJsonKeys.PRICE.value(), gson.toJsonTree(order.getStopLoss()));
            jsonOrderBody.add(BrokerJsonKeys.STOP_LOSS_ON_FILL.value(), stopLossJsonObject);
        }

        if (order.getTakeProfit() != 0.0) {
            JsonObject takeProfitJsonObject = new JsonObject();
            takeProfitJsonObject.add(BrokerJsonKeys.PRICE.value(), gson.toJsonTree(order.getTakeProfit()));
            jsonOrderBody.add(BrokerJsonKeys.TAKE_PROFIT_ON_FILL.value(), takeProfitJsonObject);
        }

        if (order.getType() == OrderType.LIMIT && order.getPrice() != 0.0) {
            jsonOrderBody.add(BrokerJsonKeys.PRICE.value(), gson.toJsonTree(order.getPrice()));
        }

        jsonObject.add(BrokerJsonKeys.ORDER.value(), jsonOrderBody);
        return jsonObject;
    }

}
