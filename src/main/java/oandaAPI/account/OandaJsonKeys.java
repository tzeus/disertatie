package oandaAPI.account;

public enum OandaJsonKeys {

	ACCOUNT("account"), ACCOUNTS("accounts"), ACCOUNT_ID("id"), ACCOUNT_CURRENCY("currency"), MARGIN_RATE("marginRate"), 
	MARGIN_USED("marginUsed"), MARGIN_AVAIL("marginAvailable"), BALANCE("balance"), UNREALIZED_PL("unrealizedPL"),
	REALIZED_PL("realizedPL"), OPEN_TRADES("openTradeCount"), INSTRUMENTS("instruments"), INSTRUMENT("instrument"),
	INTEREST_RATE("interestRate"), DISCONNECT("disconnect"), PIP("pipLocation"), BID("bid"), ASK("ask"), HEARTBEAT("heartbeat"),
	CANDLES("candles"), CODE("code"), OPEN_MID("openMid"), HIGH_MID("highMid"), LOW_MID("lowMid"), CLOSE_MID("closeMid"),
	TIME("time"), TICK("tick"), PRICES("prices"), TRADES("trades"), TRADE_ID("tradeId"), PRICE("price"), 
	AVG_PRICE("avgPrice"), ID("id"), STOP_LOSS("stopLoss"), TAKE_PROFIT("takeProfit"), UNITS("units"), SIDE("side"), 
	TYPE("type"), ORDERS("orders"), ORDER_ID("orderId"), POSITIONS("positions"), EXPIRY("expiry"), TRADE_OPENED("tradeOpened"),
	ORDER_OPENED("orderOpened"), TRANSACTION("transaction"), PL("pl"), INTEREST("interest"), ACCOUNT_BALANCE("accountBalance"), 
	NAME("name"), BIDS("bids"), ASKS("asks"), LIQUIDITY("liquidity");
	
	private String value;

	OandaJsonKeys(String value) {
		this.value = value;
	}
	
	public String value() {
		return value;
	}

}
