/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import java.util.Collection;

import oandaAPI.account.OandaAccountDataProviderService;

import oandaAPI.trade.OandaTradeManagementProvider;

import oandaAPI.util.OandaTestConstants;

import org.apache.log4j.Logger;

import org.junit.Test;

import tradingAPI.account.AccountDataProvider;
import tradingAPI.account.BaseTradingConfig;

import tradingAPI.instruments.TradeableInstrument;

import tradingAPI.trade.Trade;
import tradingAPI.trade.TradeInfoService;
import tradingAPI.trade.TradeManagementProvider;


public class TradeInfoServiceTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(TradeInfoServiceTest.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void tradeInfoServiceTest() {
        String url = OandaTestConstants.URL;
        String userName = OandaTestConstants.USERNAME;
        String accessToken = OandaTestConstants.BAGROV_TOKEN;
        AccountDataProvider<String> accountDataProvider = new OandaAccountDataProviderService(url, userName, accessToken);
        BaseTradingConfig tradingConfig = new BaseTradingConfig();
        tradingConfig.setMinReserveRatio(0.05);
        tradingConfig.setMinAmountRequired(100.00);
        tradingConfig.setMaxAllowedQuantity(10);
        TradeManagementProvider<String, String, String> tradeManagementProvider = new OandaTradeManagementProvider(url, accessToken);
        TradeInfoService<String, String, String> tradeInfoService = new TradeInfoService<String, String, String>(tradeManagementProvider, accountDataProvider);

        tradeInfoService.init();
        Collection<Trade<String, String, String>> allTrades = tradeInfoService.getAllTrades();
        LOG.info("################ Dumping All Trades ################");
        for (Trade<String, String, String> trade : allTrades) {
            LOG.info(String.format("Units=%s,Side=%s,Instrument=%s,Price=%2.5f", trade.getUnits(), trade.getSide(), trade.getInstrument().getInstrument(), trade.getExecutionPrice()));
        }
        int gbpTrades = tradeInfoService.findNetPositionCountForCurrency("GBP");
        int usdTrades = tradeInfoService.findNetPositionCountForCurrency("USD");
        LOG.info("Net Position for GBP = " + gbpTrades);
        LOG.info("Net Position for USD = " + usdTrades);
        TradeableInstrument<String> gbpusd = new TradeableInstrument<String>("GBP_USD");
        TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");
        boolean isCadChdTradeExists = tradeInfoService.isTradeExistsForInstrument(gbpusd);
        boolean isUsdCadTradeExists = tradeInfoService.isTradeExistsForInstrument(eurusd);
        LOG.info(gbpusd.getInstrument() + " exists?" + isCadChdTradeExists);
        LOG.info(eurusd.getInstrument() + " exists?" + isUsdCadTradeExists);

    }

    private static void usage(String[] args) {
        if (args.length != 3) {
            LOG.error("Usage: TradeInfoServiceDemo <url> <username> <accesstoken>");
            System.exit(1);
        }
    }

}
