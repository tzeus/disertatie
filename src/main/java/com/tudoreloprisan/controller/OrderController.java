/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.tudoreloprisan.controller;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tudoreloprisan.brokerAPI.account.BrokerAccountDataProviderService;
import com.tudoreloprisan.brokerAPI.account.BrokerProviderHelper;
import com.tudoreloprisan.brokerAPI.account.OrderType;
import com.tudoreloprisan.brokerAPI.events.OrderEvents;
import com.tudoreloprisan.brokerAPI.market.BrokerCurrentPriceInfoProvider;
import com.tudoreloprisan.brokerAPI.marketData.BrokerHistoricMarketDataProvider;
import com.tudoreloprisan.brokerAPI.order.BrokerOrderManagementProvider;
import com.tudoreloprisan.brokerAPI.trade.BrokerTradeManagementProvider;
import com.tudoreloprisan.tradingAPI.account.*;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.market.CurrentPriceInfoProvider;
import com.tudoreloprisan.tradingAPI.marketData.HistoricMarketDataProvider;
import com.tudoreloprisan.tradingAPI.marketData.MovingAverageCalculationService;
import com.tudoreloprisan.tradingAPI.order.*;
import com.tudoreloprisan.tradingAPI.trade.TradeInfoService;
import com.tudoreloprisan.tradingAPI.trade.TradeManagementProvider;
import com.tudoreloprisan.tradingAPI.trade.TradingDecision;
import com.tudoreloprisan.tradingAPI.trade.TradingSignal;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@PropertySource("classpath:auth.properties")
public class OrderController {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(OrderController.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Instance fields 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Value("${broker.url}")
    private String url; //  = env.getProperty("broker.url");
    @Value("${broker.user}")
    private String user; // =env.getProperty("broker.user");
    @Value("${broker.accessToken}")
    private String accessToken; // =env.getProperty("broker.accessToken");
    @Value("${broker.accountId}")
    private String accountId; // =env.getProperty("broker.accountId");

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @RequestMapping(value = "/getOrders", method = RequestMethod.GET)
    public String getOrders() {
        AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);

        OrderManagementProvider<String, String, String> orderManagementProvider = new BrokerOrderManagementProvider(url, accessToken, accountDataProvider);

        OrderInfoService<String, String, String> orderInfoService = new OrderInfoService<String, String, String>(orderManagementProvider);

        Collection<Order<String, String>> orders = orderInfoService.allPendingOrders();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        return gson.toJson(orders);
    }

    @RequestMapping(value = "/placeOrder", method = RequestMethod.POST)
    public String placeOrder(@RequestParam(value = "units") Integer units,
        @RequestParam(value = "instrument") String instrument,
        @RequestParam(value = "timeInForce") String timeInForce,
        @RequestParam(value = "type") String type,
        @RequestParam(value = "positionFill") String positionFill,
        @RequestParam(value = "body") String body,
        @RequestParam(value = "entryPoint") String entryPoint,
        @RequestParam(value = "direction") String direction,
        @RequestParam(value = "stopLossOnFill") String stopLossOnFill,
        @RequestParam(value = "orderType") String orderType,
        @RequestParam(value = "takeProfitOnFill") String takeProfitOnFill) throws InterruptedException {

        BlockingQueue<TradingDecision<String>> orderQueue = new LinkedBlockingQueue<TradingDecision<String>>();

        AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);
        CurrentPriceInfoProvider<String, String> currentPriceInfoProvider = new BrokerCurrentPriceInfoProvider(url, accessToken);
        BaseTradingConfig tradingConfig = new BaseTradingConfig();
        tradingConfig.setMinReserveRatio(0.05);
        tradingConfig.setMinAmountRequired(100.00);
        tradingConfig.setMaxAllowedQuantity(10);
        ProviderHelper<String> providerHelper = new BrokerProviderHelper();
        AccountInfoService<String, String> accountInfoService = new AccountInfoService<String, String>(accountDataProvider, currentPriceInfoProvider, tradingConfig, providerHelper);

        OrderManagementProvider<String, String, String> orderManagementProvider = new BrokerOrderManagementProvider(url, accessToken, accountDataProvider);

        TradeManagementProvider<String, String, String> tradeManagementProvider = new BrokerTradeManagementProvider(url, accessToken);

        OrderInfoService<String, String, String> orderInfoService = new OrderInfoService<String, String, String>(orderManagementProvider);

        TradeInfoService<String, String, String> tradeInfoService = new TradeInfoService<String, String, String>(tradeManagementProvider, accountDataProvider);

        HistoricMarketDataProvider<String> historicMarketDataProvider = new BrokerHistoricMarketDataProvider(url, accessToken);

        MovingAverageCalculationService<String> movingAverageCalculationService = new MovingAverageCalculationService<String>(historicMarketDataProvider);

        PreOrderValidationService<String, String, String> preOrderValidationService = new PreOrderValidationService<String, String, String>(tradeInfoService, movingAverageCalculationService, tradingConfig, orderInfoService);

        OrderExecutionService<String, String, String> orderExecService = new OrderExecutionService<String, String, String>(orderQueue, accountInfoService, orderManagementProvider, tradingConfig, preOrderValidationService,
            currentPriceInfoProvider);
        orderExecService.init();
        TradingDecision<String> decision = null;
        switch (OrderEvents.valueOf(orderType)) {

        case MARKET_ORDER:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        case LIMIT_ORDER:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        case TAKE_PROFIT_ORDER:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        case STOP_LOSS_ORDER:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        case MARKET_IF_TOUCHED_ORDER:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        case ORDER_FILL:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        default:
            break;
        }

//              decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument),
//                              TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill), Double.parseDouble(entryPoint));
        orderQueue.offer(decision);
        Thread.sleep(10000); // enough time to place an order
        orderExecService.shutDown();

        return "";

    }

}
