package com.tudoreloprisan.twitter;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;

public abstract class FXTradeTweet<T> {
	private final TradeableInstrument<T>	instrument;
	private final double					price;

	public FXTradeTweet(TradeableInstrument<T> instrument, double price) {
		super();
		this.instrument = instrument;
		this.price = price;
	}

	public TradeableInstrument<T> getInstrument() {
		return instrument;
	}

	public double getPrice() {
		return price;
	}

}
