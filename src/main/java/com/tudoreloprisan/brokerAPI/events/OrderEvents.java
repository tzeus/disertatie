package com.tudoreloprisan.brokerAPI.events;

import com.tudoreloprisan.tradingAPI.events.Event;

public enum OrderEvents implements Event {
	MARKET_ORDER,
	STOP_ORDER,
	LIMIT_ORDER,
	MARKET_IF_TOUCHED_ORDER,	
	ORDER_CANCEL,
	ORDER_FILL,
	TAKE_PROFIT_ORDER,
	STOP_LOSS_ORDER;
	

}
