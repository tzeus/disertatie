package com.tudoreloprisan.tradingAPI.trade;

public enum TradingSignal {
	LONG, SHORT, NONE;

	public TradingSignal flip() {
		switch (this) {
		case LONG:
			return SHORT;
		case SHORT:
			return LONG;
		default:
			return this;
		}
	}
}
