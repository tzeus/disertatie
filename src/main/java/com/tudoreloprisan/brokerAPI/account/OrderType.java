package com.tudoreloprisan.brokerAPI.account;

public enum OrderType {
	ORDER_MARKET,
	ORDER_LIMIT	,
	ORDER_MARKET_IF_TOUCHED,
	STOP,
	STOP_LOSS,
	TAKE_PROFIT,
	TRAILING_STOP_LOSS,
	FIXED_PRICE
}
