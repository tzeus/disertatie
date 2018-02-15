package twitter;

import tradingAPI.instruments.TradeableInstrument;

public class CloseFXTradeTweet<T> extends FXTradeTweet<T> {
	private final double profit, price;

	public CloseFXTradeTweet(TradeableInstrument<T> instrument, double profit, double price) {
		super(instrument, price);
		this.profit = profit;
		this.price = price;
	}

	public double getProfit() {
		return profit;
	}

	@Override
	public double getPrice() {
		return price;
	}

}
