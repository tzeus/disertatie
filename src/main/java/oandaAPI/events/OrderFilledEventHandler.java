package oandaAPI.events;

import java.util.Set;

import org.json.simple.JSONObject;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import oandaAPI.account.OandaJsonKeys;
import tradingAPI.events.EventHandler;
import tradingAPI.events.EventPayLoad;
import tradingAPI.trade.TradeInfoService;

public class OrderFilledEventHandler
		implements EventHandler<JSONObject, OrderEventPayLoad> {
	private final Set<OrderEvents>						orderEventsSupported	= Sets
			.newHashSet(OrderEvents.ORDER_FILL);
	private final TradeInfoService<String, String, String>	tradeInfoService;

	public OrderFilledEventHandler(TradeInfoService<String, String, String> tradeInfoService) {
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

		String accountId = (String) jsonPayLoad.get(OandaJsonKeys.ACCOUNT_ID.value());
		tradeInfoService.refreshTradesForAccount(accountId);
	}
}
