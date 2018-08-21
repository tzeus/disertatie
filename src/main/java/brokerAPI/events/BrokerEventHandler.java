package brokerAPI.events;

import java.util.Set;

import org.joda.time.DateTime;
import org.json.simple.JSONObject;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import email.EmailContentGenerator;
import email.EmailPayLoad;
import brokerAPI.account.BrokerJsonKeys;
import tradingAPI.events.EventHandler;
import tradingAPI.events.EventPayLoad;
import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.trade.TradeInfoService;

public class BrokerEventHandler
		implements EventHandler<JSONObject, OrderEventPayLoad>, EmailContentGenerator<JSONObject>{
	private final Set<OrderEvents>						orderEventsSupported	= Sets
			.newHashSet(OrderEvents.ORDER_FILL);
	private final TradeInfoService<String, String, String>	tradeInfoService;

	public BrokerEventHandler(TradeInfoService<String, String, String> tradeInfoService) {
		this.tradeInfoService = tradeInfoService;
	}

	@Override
	@Subscribe
	@AllowConcurrentEvents
	public void handleEvent(OrderEventPayLoad payLoad) {
		Preconditions.checkNotNull(payLoad);
		if (!orderEventsSupported.contains(payLoad.getEvent())) {
			return;
		}
		JSONObject jsonPayLoad = payLoad.getPayLoad();

		String accountId = (String) jsonPayLoad.get(BrokerJsonKeys.ACCOUNT_ID.value());
		tradeInfoService.refreshTradesForAccount(accountId);
	}

	@Override
	public EmailPayLoad generate(EventPayLoad<JSONObject> payLoad) {
		JSONObject jsonPayLoad = payLoad.getPayLoad();
		final String type = jsonPayLoad.get(BrokerJsonKeys.TYPE.value()).toString();
		
		if (type.equals(BrokerJsonKeys.HEARTBEAT.toString())) { //skip heartbeat events
			return null;
		}
		
		String emailMsg="";
		String subject="";			
		
		if (type.equals(OrderEvents.ORDER_FILL.toString())) {
			TradeableInstrument<String> instrument = new TradeableInstrument<String>(
					jsonPayLoad.get(BrokerJsonKeys.INSTRUMENT.value()).toString());
			String accountId = (String) jsonPayLoad.get(BrokerJsonKeys.ACCOUNT_ID.value());
			String tradeUnits = (String) jsonPayLoad.get(BrokerJsonKeys.UNITS.value());
			DateTime time = DateTime.parse((String) jsonPayLoad.get(BrokerJsonKeys.TIME.value()));
			
			String price = (String) jsonPayLoad.get(BrokerJsonKeys.PRICE.value());
			String accountBalance = (String) jsonPayLoad.get(BrokerJsonKeys.ACCOUNT_BALANCE.value());
			
			emailMsg = String.format(
					"Trade event %s received for account %s. Trade Units=%s. Time=%s. Price=$s. Account balance=$s",
					type, accountId, tradeUnits, time.toString(), price, accountBalance);
			subject = String.format("Trade event %s for %s", type, instrument.getInstrument());	
		}
		
		if (type.equals(OrderEvents.MARKET_ORDER.toString())) {
			TradeableInstrument<String> instrument = new TradeableInstrument<String>(
					jsonPayLoad.get(BrokerJsonKeys.INSTRUMENT.value()).toString());
			String accountId = (String) jsonPayLoad.get(BrokerJsonKeys.ACCOUNT_ID.value());
			String tradeUnits = (String) jsonPayLoad.get(BrokerJsonKeys.UNITS.value());
			DateTime time = DateTime.parse((String) jsonPayLoad.get(BrokerJsonKeys.TIME.value()));
			
			JSONObject takeProfit = (JSONObject) jsonPayLoad.get(BrokerJsonKeys.TAKE_PROFIT_ON_FILL.value());
			String priceTP=null;
			String priceSL=null;
			if(takeProfit!=null) {
				priceTP = (String) takeProfit.get(BrokerJsonKeys.PRICE.value());
			}
			JSONObject stopLoss = (JSONObject) jsonPayLoad.get(BrokerJsonKeys.STOP_LOSS_ON_FILL.value());
			if(stopLoss!=null) {
				priceSL = (String) stopLoss.get(BrokerJsonKeys.PRICE.value());
			}
			
			emailMsg = String.format(
					"Trade event %s received for account %s. Trade Units=%s. Time=%s. TakeProfit=%s. StopLoss=$s",
					type, accountId, tradeUnits, time.toString(), priceTP==null?"0":priceTP, 
							priceSL==null?"0":priceTP);
			subject = String.format("Trade event %s for %s", type, instrument.getInstrument());			
		}		
		
		if (type.equals(OrderEvents.TAKE_PROFIT_ORDER.toString())) {
			String accountId = (String) jsonPayLoad.get(BrokerJsonKeys.ACCOUNT_ID.value());
			DateTime time = DateTime.parse((String) jsonPayLoad.get(BrokerJsonKeys.TIME.value()));
			
			String price = (String) jsonPayLoad.get(BrokerJsonKeys.PRICE.value());
			
			emailMsg = String.format(
					"Trade event %s received for account %s. Time=%s. Price=$s",
					type, accountId, time.toString(), price);
			subject = String.format("Trade event %s", type);	
		}
		
		if (type.equals(OrderEvents.STOP_LOSS_ORDER.toString())) {
			String accountId = (String) jsonPayLoad.get(BrokerJsonKeys.ACCOUNT_ID.value());
			DateTime time = DateTime.parse((String) jsonPayLoad.get(BrokerJsonKeys.TIME.value()));
			
			String price = (String) jsonPayLoad.get(BrokerJsonKeys.PRICE.value());
			
			emailMsg = String.format(
					"Trade event %s received for account %s. Time=%s. Price=$s",
					type, accountId, time.toString(), price);
			subject = String.format("Trade event %s", type);
		}	

		return new EmailPayLoad(subject, emailMsg);
	}
}
