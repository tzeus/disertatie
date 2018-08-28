package com.tudoreloprisan.tradingAPI.account;

/**
 * Stores account information
 */
public class Account<T> {

	private final double	totalBalance;
	private final double	unrealisedPnl;
	private final double	realisedPnl;
	private final double	marginUsed;
	private final double	marginAvailable;
	private final double	netAssetValue;
	private final double	amountAvailableRatio;
	private final double	marginRate;
	private final long		openTrades;
	private final String	currency;
	private final T			accountId;
	private final transient String	toStr;
	private final int		hash;

	public Account(final double totalBalance, double marginAvailable, String currency, T accountId, double marginRate) {
		this(totalBalance, 0, 0, 0, marginAvailable, 0, currency, accountId, marginRate);
	}

	public Account(final double totalBalance, double unrealisedPnl, double realisedPnl, double marginUsed,
			double marginAvailable, long openTrades, String currency, T accountId, double marginRate) {
		this.totalBalance = totalBalance;
		this.unrealisedPnl = unrealisedPnl;
		this.realisedPnl = realisedPnl;
		this.marginUsed = marginUsed;
		this.marginAvailable = marginAvailable;
		this.openTrades = openTrades;
		this.currency = currency;
		this.accountId = accountId;
		this.amountAvailableRatio = this.marginAvailable / this.totalBalance;
		this.netAssetValue = this.marginUsed + this.marginAvailable;
		this.marginRate = marginRate;
		this.hash = calcHashCode();
		toStr = String.format(
				"Currency=%s,NAV=%5.2f,Total Balance=%5.2f, UnrealisedPnl=%5.2f, "
						+ "RealisedPnl=%5.2f, MarginUsed=%5.2f, MarginAvailable=%5.2f,"
						+ " OpenTrades=%d, amountAvailableRatio=%1.2f, marginRate=%1.2f",
				currency, netAssetValue, totalBalance, unrealisedPnl, realisedPnl, marginUsed, marginAvailable,
				openTrades, this.amountAvailableRatio, this.marginRate);

	}

	public double getAmountAvailableRatio() {
		return amountAvailableRatio;
	}

	public double getMarginRate() {
		return marginRate;
	}

	@Override
	public String toString() {
		return this.toStr;
	}

	public T getAccountId() {
		return accountId;
	}

	public String getCurrency() {
		return currency;
	}

	public double getNetAssetValue() {
		return this.netAssetValue;
	}

	public double getTotalBalance() {
		return totalBalance;
	}

	public double getUnrealisedPnl() {
		return unrealisedPnl;
	}

	public double getRealisedPnl() {
		return realisedPnl;
	}

	public double getMarginUsed() {
		return marginUsed;
	}

	@Override
	public int hashCode() {
		return this.hash;
	}

	private int calcHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
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
		Account<T> other = (Account<T>) obj;
		if (accountId == null) {
			if (other.accountId != null)
				return false;
		} else if (!accountId.equals(other.accountId))
			return false;
		return true;
	}

	public double getMarginAvailable() {
		return marginAvailable;
	}

	public long getOpenTrades() {
		return openTrades;
	}

}