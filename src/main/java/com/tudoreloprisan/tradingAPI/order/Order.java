package com.tudoreloprisan.tradingAPI.order;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.trade.TradingSignal;

public class Order<M, N> {
	private final TradeableInstrument<M> instrument;
	private final String units;
	private final TradingSignal side;
	private final OrderType type;
	private double takeProfit;
	private double stopLoss;
	private N orderId;
	private double price;

	
	public Order(TradeableInstrument<M> instrument, String units, TradingSignal side, OrderType type, double price) {
		this(instrument, units, side, type, 0.0, 0.0, price);
	}
//
//	public Order(TradeableInstrument<M> instrument, String units, TradingSignal side, OrderType type) {
//		this(instrument, units, side, type, 0.0, 0.0);
//	}

	public Order(TradeableInstrument<M> instrument, String units, TradingSignal side, OrderType type) {
		this.instrument = instrument;
		this.units = units;
		this.side = side;
		this.type = type;
	}

	public Order(TradeableInstrument<M> instrument, String units, TradingSignal side, OrderType type, double takeProfit,
			double stopLoss) {
		this(instrument, units, side, type, takeProfit, stopLoss, 0.0);
	}

	public Order(TradeableInstrument<M> instrument, String units, TradingSignal side, OrderType type, double takeProfit,
			double stopLoss, double price) {
		this.instrument = instrument;
		this.units = units;
		this.side = side;
		this.type = type;
		this.takeProfit = takeProfit;
		this.stopLoss = stopLoss;
		this.price = price;
	}

	public N getOrderId() {
		return orderId;
	}

	public void setOrderId(N orderId) {
		this.orderId = orderId;
	}

	public double getStopLoss() {
		return stopLoss;
	}

	public double getPrice() {
		return price;
	}

	public TradeableInstrument<M> getInstrument() {
		return instrument;
	}

	public String getUnits() {
		return getSide()==TradingSignal.LONG?units:"-"+units;
	}

	public TradingSignal getSide() {
		return side;
	}

	public OrderType getType() {
		return type;
	}

	public double getTakeProfit() {
		return takeProfit;
	}

	@Override
	public String toString() {
		return "Order [instrument=" + instrument + ", units=" + units + ", side=" + side + ", type=" + type
				+ ", takeProfit=" + takeProfit + ", stopLoss=" + stopLoss + ", orderId=" + orderId + ", price=" + price
				+ "]";
	}
}
