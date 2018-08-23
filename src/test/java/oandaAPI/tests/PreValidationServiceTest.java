/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import oandaAPI.account.OandaAccountDataProviderService;

import oandaAPI.marketData.OandaHistoricMarketDataProvider;

import oandaAPI.order.OandaOrderManagementProvider;

import oandaAPI.trade.OandaTradeManagementProvider;

import oandaAPI.util.OandaTestConstants;

import org.apache.log4j.Logger;

import org.junit.Test;

import tradingAPI.account.AccountDataProvider;
import tradingAPI.account.BaseTradingConfig;

import tradingAPI.instruments.TradeableInstrument;

import tradingAPI.marketData.HistoricMarketDataProvider;
import tradingAPI.marketData.MovingAverageCalculationService;

import tradingAPI.order.OrderInfoService;
import tradingAPI.order.OrderManagementProvider;
import tradingAPI.order.PreOrderValidationService;

import tradingAPI.trade.TradeInfoService;
import tradingAPI.trade.TradeManagementProvider;
import tradingAPI.trade.TradingSignal;


public class PreValidationServiceTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(PreValidationServiceTest.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void testValidation() {
        String url = OandaTestConstants.URL;
        String userName = OandaTestConstants.USERNAME;
        String accessToken = OandaTestConstants.BAGROV_TOKEN;

        AccountDataProvider<String> accountDataProvider = new OandaAccountDataProviderService(url, userName, accessToken);

        OrderManagementProvider<String, String, String> orderManagementProvider = new OandaOrderManagementProvider(url, accessToken, accountDataProvider);

        TradeManagementProvider<String, String, String> tradeManagementProvider = new OandaTradeManagementProvider(url, accessToken);

        BaseTradingConfig tradingConfig = new BaseTradingConfig();
        tradingConfig.setMinReserveRatio(0.05);
        tradingConfig.setMinAmountRequired(100.00);
        tradingConfig.setMaxAllowedQuantity(10);
        tradingConfig.setMaxAllowedNetContracts(3);

        TradeInfoService<String, String, String> tradeInfoService = new TradeInfoService<String, String, String>(tradeManagementProvider, accountDataProvider);

        tradeInfoService.init();

        HistoricMarketDataProvider<String> historicMarketDataProvider = new OandaHistoricMarketDataProvider(url, accessToken);

        MovingAverageCalculationService<String> movingAverageCalculationService = new MovingAverageCalculationService<String>(historicMarketDataProvider);

        OrderInfoService<String, String, String> orderInfoService = new OrderInfoService<String, String, String>(orderManagementProvider);

        PreOrderValidationService<String, String, String> preOrderValidationService = new PreOrderValidationService<String, String, String>(tradeInfoService, movingAverageCalculationService, tradingConfig, orderInfoService);

        TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");
        TradeableInstrument<String> usdjpy = new TradeableInstrument<String>("USD_JPY");
        boolean isEurUsdTraded = preOrderValidationService.checkInstrumentNotAlreadyTraded(eurusd);
        boolean isUsdJpyTraded = preOrderValidationService.checkInstrumentNotAlreadyTraded(usdjpy);
        LOG.info(eurusd.getInstrument() + " trade present? " + !isEurUsdTraded);
        LOG.info(usdjpy.getInstrument() + " trade present? " + !isUsdJpyTraded);

        TradeableInstrument<String> usdzar = new TradeableInstrument<String>("USD_ZAR");

        boolean isUsdZarTradeInSafeZone = preOrderValidationService.isInSafeZone(TradingSignal.LONG, 17.9, usdzar);
        LOG.info(usdzar.getInstrument() + " in safe zone? " + isUsdZarTradeInSafeZone);
        boolean isEurUsdTradeInSafeZone = preOrderValidationService.isInSafeZone(TradingSignal.LONG, 1.2, eurusd);
        LOG.info(eurusd.getInstrument() + " in safe zone? " + isEurUsdTradeInSafeZone);

        TradeableInstrument<String> nzdchf = new TradeableInstrument<String>("NZD_CHF");

        preOrderValidationService.checkLimitsForCcy(nzdchf, TradingSignal.LONG);
    }

    private static void usage(String[] args) {
        if (args.length != 3) {
            LOG.error("Usage: PreValidationServiceDemo <url> <username> <accesstoken>");
            System.exit(1);
        }
    }

}
