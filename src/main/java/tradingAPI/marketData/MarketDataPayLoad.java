package tradingAPI.marketData;

import org.joda.time.DateTime;

import tradingAPI.instruments.TradeableInstrument;

public class MarketDataPayLoad<T> {
	private final double					bidPrice, askPrice;
	private final TradeableInstrument<T>	instrument;
	private final DateTime					eventDate;

	public MarketDataPayLoad(TradeableInstrument<T> instrument, double bidPrice, double askPrice, DateTime eventDate) {
		this.bidPrice = bidPrice;
		this.askPrice = askPrice;
		this.instrument = instrument;
		this.eventDate = eventDate;
	}

	public double getBidPrice() {
		return bidPrice;
	}

	public double getAskPrice() {
		return askPrice;
	}

	public TradeableInstrument<T> getInstrument() {
		return instrument;
	}

	public DateTime getEventDate() {
		return eventDate;
	}
}
