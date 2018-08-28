package com.tudoreloprisan.brokerAPI.order;

import java.util.Collection;

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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
		JSONObject jsonObject = getJSONForNewOrder(order);
		StringEntity jsonStringEntity = new StringEntity(jsonObject.toJSONString());
		httpPut.setEntity(jsonStringEntity);			
		return httpPut;
	}

	HttpPost createPostCommand(Order<String, String> order, String accountId) throws Exception {
		HttpPost httpPost = new HttpPost(this.url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH
				+ accountId + BrokerConstants.ORDERS_RESOURCE);
		httpPost.setHeader(this.authHeader);
		httpPost.setHeader("content-type", "application/json");
		JSONObject jsonObject = getJSONForNewOrder(order);
		StringEntity jsonStringEntity = new StringEntity(jsonObject.toJSONString());
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
					Object o = JSONValue.parse(strResp);
					JSONObject orderResponse = (JSONObject) ((JSONObject) o).get(BrokerJsonKeys.
							ORDER_CREATE_TRANSACTION.value());
					String orderId = (String) orderResponse.get(BrokerJsonKeys.ID.value());
					LOG.info("Order executed->" + strResp);
					return orderId;
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
	public Order<String, String> pendingOrderForAccount(String orderId, String accountId) {
		CloseableHttpClient httpClient = getHttpClient();
		try {
			HttpUriRequest httpGet = new HttpGet(orderForAccountUrl(accountId, orderId));
			httpGet.setHeader(this.authHeader);
			httpGet.setHeader(BrokerConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				JSONObject order = (JSONObject) JSONValue.parse(strResp);
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

	private Order<String, String> parseOrder(JSONObject order) {
		LOG.info(String.format("Parsing order... %s", order.toJSONString()));
		
		final OrderType orderType = BrokerUtils.toOrderType((String) order.get(BrokerJsonKeys.TYPE.value()));
		if(orderType==OrderType.STOP_LOSS||orderType==OrderType.TAKE_PROFIT) {
			return null;			
		}	
		final String orderInstrument = (String) order.get(BrokerJsonKeys.INSTRUMENT.value());
			
		final String orderUnits = (String) order.get(BrokerJsonKeys.UNITS.value());
		final TradingSignal orderSide = orderUnits.startsWith("-")?TradingSignal.SHORT:TradingSignal.LONG;
		
		
		
		JSONObject takeProfitJson = (JSONObject) order.get(BrokerJsonKeys.TAKE_PROFIT_ON_FILL.value());
		final double orderTakeProfit = takeProfitJson==null?0:(Double.valueOf((String) takeProfitJson.get(BrokerJsonKeys.PRICE.value())));
		
		JSONObject stopLossJson = (JSONObject) order.get(BrokerJsonKeys.STOP_LOSS_ON_FILL.value());
		final double orderStopLoss = stopLossJson==null?0:Double.valueOf((String) stopLossJson.get(BrokerJsonKeys.PRICE.value()));
		
		final double orderPrice = Double.valueOf((String) order.get(BrokerJsonKeys.PRICE.value()));
		
		String orderId = (String) order.get(BrokerJsonKeys.ID.value());
		
		
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
					(instrument==null?(BrokerConstants.PENDING_ORDERS_RESOURCE):(BrokerConstants.ORDERS_RESOURCE +
							"?instrument=" + instrument.getInstrument())));
					
					
			httpGet.setHeader(this.authHeader);
			httpGet.setHeader(BrokerConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				JSONArray accountOrders = (JSONArray) jsonResp.get(BrokerJsonKeys.ORDERS.value());
				for (Object o : accountOrders) {
					JSONObject order = (JSONObject) o;
					Order<String, String> pendingOrder = parseOrder(order);
					if(pendingOrder!=null)
						pendingOrders.add(pendingOrder);
				}
			} else {
				TradingUtils.printErrorMsg(resp);
			}
		} catch (Exception e) {
			LOG.error(String.format("error encountered whilst fetching pending orders for account %s and instrument %s",
					accountId, instrument==null?"NONE":instrument.getInstrument()), e);
		} finally {
			TradingUtils.closeSilently(httpClient);
		}
		return pendingOrders;
	}

	@Override
	public Collection<Order<String, String>> pendingOrdersForAccount(String accountId) {
		return this.pendingOrdersForAccount(accountId, null);
	}

	String orderForAccountUrl(String accountId, String orderId) {
		return this.url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId +
				BrokerConstants.ORDERS_RESOURCE + TradingConstants.FWD_SLASH + orderId;
	}
	
	String cancelOrderForAccountUrl(String accountId, String orderId) {
		return this.url + BrokerConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId +
				BrokerConstants.ORDERS_RESOURCE + TradingConstants.FWD_SLASH + orderId+BrokerConstants.CANCEL_RESOURCE;
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
				Object o = JSONValue.parse(strResp);
				JSONObject orderResponse = (JSONObject) ((JSONObject) o).get(BrokerJsonKeys.
						ORDER_CREATE_TRANSACTION.value());
				String orderId = (String) orderResponse.get(BrokerJsonKeys.ID.value());
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
	
	@SuppressWarnings("unchecked")// JSONObject contains strings as well as other JSONObjects
	private JSONObject getJSONForNewOrder(Order<String, String> order) {
		JSONObject jsonObject = new JSONObject();
		JSONObject jsonOrderBody = new JSONObject();
		jsonOrderBody.put(BrokerJsonKeys.INSTRUMENT.value(), order.getInstrument().getInstrument());
		jsonOrderBody.put(BrokerJsonKeys.UNITS.value(), String.valueOf(order.getUnits()));
		jsonOrderBody.put(BrokerJsonKeys.TYPE.value(), order.getType().toString());
		
		JSONObject stopLossJsonObject = new JSONObject();
		stopLossJsonObject.put(BrokerJsonKeys.PRICE.value(), String.valueOf(order.getStopLoss()));
		jsonOrderBody.put(BrokerJsonKeys.STOP_LOSS_ON_FILL.value(), stopLossJsonObject);
		
		JSONObject takeProfitJsonObject = new JSONObject();
		takeProfitJsonObject.put(BrokerJsonKeys.PRICE.value(), String.valueOf(order.getTakeProfit()));
		jsonOrderBody.put(BrokerJsonKeys.TAKE_PROFIT_ON_FILL.value(), takeProfitJsonObject);
		
		if (order.getType() == OrderType.LIMIT && order.getPrice() != 0.0) {
			jsonOrderBody.put(BrokerJsonKeys.PRICE.value(), String.valueOf(order.getPrice()));
		}
		
		jsonObject.put(BrokerJsonKeys.ORDER.value(), jsonOrderBody);
		return jsonObject;
	}

}
