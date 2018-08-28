package com.tudoreloprisan.tradingAPI.market;

import org.joda.time.DateTime;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;

public class Price<T> {
	private final TradeableInstrument<T> instrument;
	private final double bidPrice, askPrice;
	private final DateTime pricePoint;

	public TradeableInstrument<T> getInstrument() {
		return instrument;
	}

	public double getBidPrice() {
		return bidPrice;
	}

	public double getAskPrice() {
		return askPrice;
	}

	public DateTime getPricePoint() {
		return pricePoint;
	}

	public Price(TradeableInstrument<T> instrument, double bidPrice, double askPrice, DateTime pricePoint) {
		this.instrument = instrument;
		this.bidPrice = bidPrice;
		this.askPrice = askPrice;
		this.pricePoint = pricePoint;
	}
}
