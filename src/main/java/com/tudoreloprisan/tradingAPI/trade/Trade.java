package com.tudoreloprisan.tradingAPI.trade;

import org.joda.time.DateTime;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;

public class Trade<M, N, K> {
	private final M							tradeId;
	private final String					units;
	private final TradingSignal				side;
	private final TradeableInstrument<N>	instrument;
	private final DateTime					tradeDate;
	private final double					takeProfitPrice, executionPrice, stopLoss;
	private final K							accountId;
	private final transient String					toStr;

	public Trade(M tradeId, String units, TradingSignal side, TradeableInstrument<N> instrument, DateTime tradeDate,
			double takeProfitPrice, double executionPrice, double stopLoss, K accountId) {
		this.tradeId = tradeId;
		this.units = units;
		this.side = side;
		this.instrument = instrument;
		this.tradeDate = tradeDate;
		this.takeProfitPrice = takeProfitPrice;
		this.executionPrice = executionPrice;
		this.stopLoss = stopLoss;
		this.accountId = accountId;
		this.toStr = String.format(
				"Trade Id=%s, Units=%s, Side=%s, Instrument=%s, TradeDate=%s, TP=%3.5f, Price=%3.5f, SL=%3.5f", tradeId,
				units, side, instrument, tradeDate.toString(), takeProfitPrice, executionPrice, stopLoss);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
		result = prime * result + ((tradeId == null) ? 0 : tradeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		Trade<M, N, K> other = (Trade<M, N, K>) obj;
		if (accountId == null) {
			if (other.accountId != null)
				return false;
		} else if (!accountId.equals(other.accountId))
			return false;
		if (tradeId == null) {
			if (other.tradeId != null)
				return false;
		} else if (!tradeId.equals(other.tradeId))
			return false;
		return true;
	}

	public double getStopLoss() {
		return stopLoss;
	}

	public K getAccountId() {
		return accountId;
	}

	@Override
	public String toString() {
		return toStr;
	}

	public double getExecutionPrice() {
		return executionPrice;
	}

	public M getTradeId() {
		return tradeId;
	}

	public String getUnits() {
		return units;
	}

	public TradingSignal getSide() {
		return side;
	}

	public TradeableInstrument<N> getInstrument() {
		return instrument;
	}

	public DateTime getTradeDate() {
		return tradeDate;
	}

	public double getTakeProfitPrice() {
		return takeProfitPrice;
	}
}
