/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.tudoreloprisan.tradingAPI.account;

import javax.persistence.*;

import com.google.gson.GsonBuilder;

import org.hibernate.annotations.Type;


/**
 * Stores account information
 */
@Entity
public class Account<T> {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Instance fields 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private double totalBalance;
    private double unrealisedPnl;
    private double realisedPnl;
    private double marginUsed;
    private double marginAvailable;
    private double netAssetValue;
    private double amountAvailableRatio;
    private double marginRate;
    private long openTrades;
    private String currency;
    @Type(type = "java.lang.String")
    private T accountId;
    private transient String toStr;
    private int hash;

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Constructors 
    //~ ----------------------------------------------------------------------------------------------------------------

    public Account() {
    }

    public Account(String jsonString) {
        Account account = new GsonBuilder().create().fromJson(jsonString, Account.class);

        this.totalBalance = account.getTotalBalance();
        this.unrealisedPnl = account.getUnrealisedPnl();
        this.realisedPnl = account.getRealisedPnl();
        this.marginUsed = account.getMarginUsed();
        this.marginAvailable = account.getMarginAvailable();
        this.openTrades = account.getOpenTrades();
        this.currency = account.getCurrency();
        this.accountId = ((T) account.getAccountId());
        this.amountAvailableRatio = this.marginAvailable / this.totalBalance;
        this.netAssetValue = this.marginUsed + this.marginAvailable;
        this.marginRate = account.getMarginRate();
        this.hash = calcHashCode();
    }

    public Account(final double totalBalance, double marginAvailable, String currency, T accountId, double marginRate) {
        this(totalBalance, 0, 0, 0, marginAvailable, 0, currency, accountId, marginRate);
    }

    public Account(final double totalBalance, double unrealisedPnl, double realisedPnl, double marginUsed, double marginAvailable, long openTrades, String currency, T accountId, double marginRate) {
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
        toStr = String.format("Currency=%s,NAV=%5.2f,Total Balance=%5.2f, UnrealisedPnl=%5.2f, " +
            "RealisedPnl=%5.2f, MarginUsed=%5.2f, MarginAvailable=%5.2f," +
            " OpenTrades=%d, amountAvailableRatio=%1.2f, marginRate=%1.2f", currency, netAssetValue, totalBalance, unrealisedPnl, realisedPnl, marginUsed, marginAvailable, openTrades, this.amountAvailableRatio,
            this.marginRate);

    }

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private int calcHashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((accountId == null) ? 0 : accountId.hashCode());
        return result;
    }
}
