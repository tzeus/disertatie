package com.tudoreloprisan.twitter;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.trade.TradingSignal;

public class NewFXTradeTweet<T> extends FXTradeTweet<T> {
	private final double		stopLoss, takeProfit;
	private final TradingSignal	action;
	private final String		str;

	public NewFXTradeTweet(TradeableInstrument<T> instrument, double price, double stopLoss, double takeProfit,
			TradingSignal action) {
		super(instrument, price);
		this.stopLoss = stopLoss;
		this.takeProfit = takeProfit;
		this.action = action;
		this.str = String.format("%s@%3.5f TP: %3.5f: SL: %3.5f %s", instrument.getInstrument(), price, takeProfit,
				stopLoss, action.name());
	}

	@Override
	public String toString() {
		return str;
	}

	public TradingSignal getAction() {
		return this.action;
	}

	public double getStopLoss() {
		return stopLoss;
	}

	public double getTakeProfit() {
		return takeProfit;
	}

}
