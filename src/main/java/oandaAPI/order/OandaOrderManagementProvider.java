package oandaAPI.order;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.Lists;

import oandaAPI.account.OandaConstants;
import oandaAPI.account.OandaJsonKeys;
import oandaAPI.util.OandaUtils;
import tradingAPI.account.Account;
import tradingAPI.account.AccountDataProvider;
import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.order.Order;
import tradingAPI.order.OrderManagementProvider;
import tradingAPI.order.OrderType;
import tradingAPI.trade.TradingSignal;
import tradingAPI.util.TradingConstants;
import tradingAPI.util.TradingUtils;

public class OandaOrderManagementProvider implements OrderManagementProvider<String, String, String> {

	private static final Logger LOG = Logger.getLogger(OandaOrderManagementProvider.class);

	private final String url;
	private final BasicHeader authHeader;	
	private final AccountDataProvider<String> accountDataProvider;

	public OandaOrderManagementProvider(String url, String accessToken, AccountDataProvider<String> accountDataProvider) {
		this.url = url;
		this.authHeader = OandaUtils.createAuthHeader(accessToken);
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
		HttpPost httpPost = new HttpPost(this.url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH
				+ accountId + OandaConstants.ORDERS_RESOURCE);
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
					JSONObject orderResponse = (JSONObject) ((JSONObject) o).get(OandaJsonKeys.
							ORDER_CREATE_TRANSACTION.value());
					String orderId = (String) orderResponse.get(OandaJsonKeys.ID.value());
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
			httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);
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
		
		final OrderType orderType = OandaUtils.toOrderType((String) order.get(OandaJsonKeys.TYPE.value()));	
		if(orderType==OrderType.STOP_LOSS||orderType==OrderType.TAKE_PROFIT) {
			return null;			
		}	
		final String orderInstrument = (String) order.get(OandaJsonKeys.INSTRUMENT.value());
			
		final String orderUnits = (String) order.get(OandaJsonKeys.UNITS.value());
		final TradingSignal orderSide = orderUnits.startsWith("-")?TradingSignal.SHORT:TradingSignal.LONG;
		
		
		
		JSONObject takeProfitJson = (JSONObject) order.get(OandaJsonKeys.TAKE_PROFIT_ON_FILL.value());		
		final double orderTakeProfit = takeProfitJson==null?0:(Double.valueOf((String) takeProfitJson.get(OandaJsonKeys.PRICE.value())));
		
		JSONObject stopLossJson = (JSONObject) order.get(OandaJsonKeys.STOP_LOSS_ON_FILL.value());	
		final double orderStopLoss = stopLossJson==null?0:Double.valueOf((String) stopLossJson.get(OandaJsonKeys.PRICE.value()));
		
		final double orderPrice = Double.valueOf((String) order.get(OandaJsonKeys.PRICE.value()));
		
		String orderId = (String) order.get(OandaJsonKeys.ID.value());
		
		
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
			
			HttpUriRequest httpGet = new HttpGet(this.url + OandaConstants.ACCOUNTS_RESOURCE
					+ TradingConstants.FWD_SLASH + accountId + 
					(instrument==null?(OandaConstants.PENDING_ORDERS_RESOURCE):(OandaConstants.ORDERS_RESOURCE +
							"?instrument=" + instrument.getInstrument())));
					
					
			httpGet.setHeader(this.authHeader);
			httpGet.setHeader(OandaConstants.UNIX_DATETIME_HEADER);
			LOG.info(TradingUtils.executingRequestMsg(httpGet));
			HttpResponse resp = httpClient.execute(httpGet);
			String strResp = TradingUtils.responseToString(resp);
			if (strResp != StringUtils.EMPTY) {
				Object obj = JSONValue.parse(strResp);
				JSONObject jsonResp = (JSONObject) obj;
				JSONArray accountOrders = (JSONArray) jsonResp.get(OandaJsonKeys.ORDERS.value());
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
		return this.url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + 
				OandaConstants.ORDERS_RESOURCE + TradingConstants.FWD_SLASH + orderId;
	}
	
	String cancelOrderForAccountUrl(String accountId, String orderId) {
		return this.url + OandaConstants.ACCOUNTS_RESOURCE + TradingConstants.FWD_SLASH + accountId + 
				OandaConstants.ORDERS_RESOURCE + TradingConstants.FWD_SLASH + orderId+OandaConstants.CANCEL_RESOURCE;
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
				JSONObject orderResponse = (JSONObject) ((JSONObject) o).get(OandaJsonKeys.
						ORDER_CREATE_TRANSACTION.value());
				String orderId = (String) orderResponse.get(OandaJsonKeys.ID.value());
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
		jsonOrderBody.put(OandaJsonKeys.INSTRUMENT.value(), order.getInstrument().getInstrument());
		jsonOrderBody.put(OandaJsonKeys.UNITS.value(), String.valueOf(order.getUnits()));
		jsonOrderBody.put(OandaJsonKeys.TYPE.value(), order.getType().toString());
		
		JSONObject stopLossJsonObject = new JSONObject();
		stopLossJsonObject.put(OandaJsonKeys.PRICE.value(), String.valueOf(order.getStopLoss()));
		jsonOrderBody.put(OandaJsonKeys.STOP_LOSS_ON_FILL.value(), stopLossJsonObject);
		
		JSONObject takeProfitJsonObject = new JSONObject();
		takeProfitJsonObject.put(OandaJsonKeys.PRICE.value(), String.valueOf(order.getTakeProfit()));
		jsonOrderBody.put(OandaJsonKeys.TAKE_PROFIT_ON_FILL.value(), takeProfitJsonObject);		
		
		if (order.getType() == OrderType.LIMIT && order.getPrice() != 0.0) {
			jsonOrderBody.put(OandaJsonKeys.PRICE.value(), String.valueOf(order.getPrice()));			
		}
		
		jsonObject.put(OandaJsonKeys.ORDER.value(), jsonOrderBody);
		return jsonObject;
	}

}
