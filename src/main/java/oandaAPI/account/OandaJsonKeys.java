package oandaAPI.account;

public enum OandaJsonKeys {

	ACCOUNT("account"), ACCOUNTS("accounts"), ACCOUNT_ID("id"), ACCOUNT_CURRENCY("currency"), MARGIN_RATE("marginRate"), 
	MARGIN_USED("marginUsed"), MARGIN_AVAIL("marginAvailable"), BALANCE("balance"), UNREALIZED_PL("unrealizedPL"),
	REALIZED_PL("realizedPL"), OPEN_TRADES("openTradeCount"), INSTRUMENTS("instruments"), INSTRUMENT("instrument"),
	INTEREST_RATE("interestRate"), DISCONNECT("disconnect"), PIP("pipLocation"), BID("bid"), ASK("ask"), HEARTBEAT("heartbeat"),
	CANDLES("candles"), CODE("code"), OPEN("o"), HIGH("h"), LOW("l"), CLOSE("c"),
	TIME("time"), TICK("tick"), PRICES("prices"), TRADES("trades"), TRADE_ID("tradeId"), PRICE("price"), 
	AVG_PRICE("avgPrice"), ID("id"), STOP_LOSS_ON_FILL("stopLossOnFill"), TAKE_PROFIT_ON_FILL("takeProfitOnFill"), 
	UNITS("units"), SIDE("side"), TYPE("type"), ORDERS("orders"), ORDER_ID("orderId"), POSITIONS("positions"),
	EXPIRY("expiry"), TRADE_OPENED("tradeOpened"), ORDER_OPENED("orderOpened"), TRANSACTION("transaction"), PL("pl"), 
	INTEREST("interest"), ACCOUNT_BALANCE("accountBalance"), NAME("name"), BIDS("bids"), ASKS("asks"), 
	LIQUIDITY("liquidity"), MIDPOINT("mid"), ORDER("order"), ORDER_CREATE_TRANSACTION("orderCreateTransaction"), 
	OPEN_TIME("openTime"), CURRENT_UNITS("currentUnits"), TAKE_PROFIT_ORDER("takeProfitOrder"), 
	STOP_LOSS_ORDER("stopLossOrder "), TAKE_PROFIT("takeProfit"), STOP_LOSS("stopLoss");
	
	private String value;

	OandaJsonKeys(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}

}
