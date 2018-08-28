/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.tudoreloprisan.tradingAPI.account;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.market.CurrentPriceInfoProvider;
import com.tudoreloprisan.tradingAPI.market.Price;
import com.tudoreloprisan.tradingAPI.util.TradingUtils;

import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;


@PropertySource("classpath:auth.properties")
public class AccountInfoService<K, N> {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Instance fields 
    //~ ----------------------------------------------------------------------------------------------------------------

    private final AccountDataProvider<K> accountDataProvider;
    private final BaseTradingConfig baseTradingConfig;
    private final CurrentPriceInfoProvider<N, K> currentPriceInfoProvider;
    private final ProviderHelper providerHelper;
    private Comparator<Account<K>> accountComparator = new MarginAvailableComparator<K>();

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Constructors 
    //~ ----------------------------------------------------------------------------------------------------------------

    public AccountInfoService(AccountDataProvider<K> accountDataProvider, CurrentPriceInfoProvider<N, K> currentPriceInfoProvider, BaseTradingConfig baseTradingConfig, ProviderHelper providerHelper) {
        this.accountDataProvider = accountDataProvider;
        this.baseTradingConfig = baseTradingConfig;
        this.currentPriceInfoProvider = currentPriceInfoProvider;
        this.providerHelper = providerHelper;
    }

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    public Collection<Account<K>> getAllAccounts() {
        return this.accountDataProvider.getLatestAccountsInfo();
    }

    public Account<K> getAccountInfo(K accountId) {
        return this.accountDataProvider.getLatestAccountInfo(accountId);
    }

    public Collection<K> findAccountsToTrade() {
        List<Account<K>> accounts = Lists.newArrayList(getAllAccounts());
        Collection<K> accountsFound = Lists.newArrayList();
        Collections.sort(accounts, accountComparator);
        for (Account<K> account : accounts) {
            if ((account.getAmountAvailableRatio() >= baseTradingConfig.getMinReserveRatio()) && (account.getNetAssetValue() >= baseTradingConfig.getMinAmountRequired())) {
                accountsFound.add(account.getAccountId());
            }
        }
        return accountsFound;
    }

    /*
     * ({BASE} / {Home Currency}) * units) / (margin ratio) For example,
     * suppose: Home Currency = USD Currency Pair = GBP/CHF Base = GBP; Quote =
     * CHF Base / Home Currency = GBP/USD = 1.5819 Units = 1000 Margin Ratio =
     * 20:1 Then, margin used: = (1.5819 * 1000) / 20 = 79.095 USD
     */
    @SuppressWarnings("unchecked")
    public double calculateMarginForTrade(Account<K> accountInfo, TradeableInstrument<N> instrument, int units) {
        String[] tokens = TradingUtils.splitInstrumentPair(instrument.getInstrument());
        String baseCurrency = tokens[0];
        double price = 1.0;
        System.out.println("Base currency " + baseCurrency + ".....................................................");
        if (!baseCurrency.equals(accountInfo.getCurrency())) {
            String currencyPair = this.providerHelper.fromIsoFormat(baseCurrency + accountInfo.getCurrency());
            System.out.println(currencyPair);
            Map<TradeableInstrument<N>, Price<N>> priceInfoMap = this.currentPriceInfoProvider.getCurrentPricesForInstruments(Lists.newArrayList(new TradeableInstrument<N>(currencyPair)), (K) accountInfo.getAccountId());
            if (priceInfoMap.isEmpty()) { /*
                                           * this means we got the currency
                                           * pair inverted
                                           */
                /*
                 * example when the home currency is GBP and instrument is
                 * USDJPY
                 */
                currencyPair = this.providerHelper.fromIsoFormat(accountInfo.getCurrency() + baseCurrency);
                priceInfoMap = this.currentPriceInfoProvider.getCurrentPricesForInstruments(Lists.newArrayList(new TradeableInstrument<N>(currencyPair)), (K) accountInfo.getAccountId());
                if (priceInfoMap.isEmpty()) { // something else is wrong here
                    System.out.println("Something went wrong...");
                    return Double.MAX_VALUE;
                }
                Price<N> priceInfo = priceInfoMap.values().iterator().next();
                price = 1.0 / ((priceInfo.getBidPrice() + priceInfo.getAskPrice()) / 2.0); /* take avg of bid and ask
                                                                                            * prices */
            } else {
                Price<N> priceInfo = priceInfoMap.values().iterator().next();
                price = (priceInfo.getBidPrice() + priceInfo.getAskPrice()) / 2.0; /* take avg of bid and ask prices */
            }

        }
        return price * units * accountInfo.getMarginRate();
    }

    public double calculateMarginForTrade(K accountId, TradeableInstrument<N> instrument, int units) {
        return calculateMarginForTrade(getAccountInfo(accountId), instrument, units);
    }

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Nested Classes 
    //~ ----------------------------------------------------------------------------------------------------------------

    static class MarginAvailableComparator<K> implements Comparator<Account<K>> {

        @Override
        public int compare(Account<K> ai1, Account<K> ai2) {
            if (ai1.getMarginAvailable() > ai2.getMarginAvailable()) {
                return -1;
            } else if (ai1.getMarginAvailable() < ai2.getMarginAvailable()) {
                return 1;
            }
            return 0;
        }

    }
}
