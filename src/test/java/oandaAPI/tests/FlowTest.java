/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import oandaAPI.account.OandaAccountDataProviderService;
import oandaAPI.account.OandaProviderHelper;

import oandaAPI.market.OandaCurrentPriceInfoProvider;

import oandaAPI.marketData.OandaHistoricMarketDataProvider;

import oandaAPI.order.OandaOrderManagementProvider;

import oandaAPI.trade.OandaTradeManagementProvider;

import oandaAPI.util.OandaTestConstants;

import org.junit.Test;

import tradingAPI.account.AccountDataProvider;
import tradingAPI.account.AccountInfoService;
import tradingAPI.account.BaseTradingConfig;
import tradingAPI.account.ProviderHelper;

import tradingAPI.instruments.TradeableInstrument;

import tradingAPI.market.CurrentPriceInfoProvider;

import tradingAPI.marketData.HistoricMarketDataProvider;
import tradingAPI.marketData.MovingAverageCalculationService;

import tradingAPI.order.OrderExecutionService;
import tradingAPI.order.OrderInfoService;
import tradingAPI.order.OrderManagementProvider;
import tradingAPI.order.PreOrderValidationService;

import tradingAPI.trade.TradeInfoService;
import tradingAPI.trade.TradeManagementProvider;
import tradingAPI.trade.TradingDecision;
import tradingAPI.trade.TradingSignal;


public class FlowTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void testEndToEndFlow() throws InterruptedException {

        String url = OandaTestConstants.URL;
        String userName = OandaTestConstants.USERNAME;
        String accessToken = OandaTestConstants.BAGROV_TOKEN;

        AccountDataProvider<String> accountDataProvider = new OandaAccountDataProviderService(url, userName, accessToken);
        ProviderHelper<String> providerHelper = new OandaProviderHelper();
        BaseTradingConfig tradingConfig = new BaseTradingConfig();
        tradingConfig.setMinReserveRatio(0.05);
        tradingConfig.setMinAmountRequired(100.00);
        tradingConfig.setMaxAllowedQuantity(10);
        CurrentPriceInfoProvider<String, String> currentPriceInfoProvider = new OandaCurrentPriceInfoProvider(url, accessToken);
        AccountInfoService<String, String> accountInfoService = new AccountInfoService<String, String>(accountDataProvider, currentPriceInfoProvider, tradingConfig, providerHelper);
        OrderManagementProvider<String, String, String> orderManagementProvider = new OandaOrderManagementProvider(url, accessToken, accountDataProvider);
        TradeManagementProvider<String, String, String> tradeManagementProvider = new OandaTradeManagementProvider(url, accessToken);
        OrderInfoService<String, String, String> orderInfoService = new OrderInfoService<String, String, String>(orderManagementProvider);
        TradeInfoService<String, String, String> tradeInfoService = new TradeInfoService<String, String, String>(tradeManagementProvider, accountDataProvider);
        HistoricMarketDataProvider<String> historicMarketDataProvider = new OandaHistoricMarketDataProvider(url, accessToken);
        BlockingQueue<TradingDecision<String>> orderQueue = new LinkedBlockingQueue<TradingDecision<String>>();
        MovingAverageCalculationService<String> movingAverageCalculationService = new MovingAverageCalculationService<String>(historicMarketDataProvider);
        PreOrderValidationService<String, String, String> preOrderValidationService = new PreOrderValidationService<String, String, String>(tradeInfoService, movingAverageCalculationService, tradingConfig, orderInfoService);
        OrderExecutionService<String, String, String> orderExecService = new OrderExecutionService<String, String, String>(orderQueue, accountInfoService, orderManagementProvider, tradingConfig, preOrderValidationService,
            currentPriceInfoProvider);

        orderExecService.init();

        TradingDecision<String> decision = new TradingDecision<String>(new TradeableInstrument<String>("GBP_USD"), TradingSignal.LONG, 1.48, 1.20, 1.28);
        orderQueue.offer(decision);
        Thread.sleep(10000); // enough time to place an order
        orderExecService.shutDown();

    }
}
