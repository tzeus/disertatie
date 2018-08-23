/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import java.util.Collection;

import oandaAPI.account.OandaAccountDataProviderService;

import oandaAPI.order.OandaOrderManagementProvider;

import oandaAPI.util.OandaTestConstants;

import org.apache.log4j.Logger;

import org.junit.Test;

import tradingAPI.account.AccountDataProvider;

import tradingAPI.instruments.TradeableInstrument;

import tradingAPI.order.Order;
import tradingAPI.order.OrderInfoService;
import tradingAPI.order.OrderManagementProvider;


public class OrderInfoServiceTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(OrderInfoServiceTest.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void testOrderInfo() {

        String url = OandaTestConstants.URL;
        String userName = OandaTestConstants.USERNAME;
        String accessToken = OandaTestConstants.BAGROV_TOKEN;

        AccountDataProvider<String> accountDataProvider = new OandaAccountDataProviderService(url, userName, accessToken);

        OrderManagementProvider<String, String, String> orderManagementProvider = new OandaOrderManagementProvider(url, accessToken, accountDataProvider);

        OrderInfoService<String, String, String> orderInfoService = new OrderInfoService<String, String, String>(orderManagementProvider);

        TradeableInstrument<String> gbpusd = new TradeableInstrument<String>("GBP_USD");

        orderInfoService.allPendingOrders();
        Collection<Order<String, String>> pendingOrdersGbpUsd = orderInfoService.pendingOrdersForInstrument(gbpusd);

        LOG.info(String.format("+++++++++++++++++++ Dumping all pending orders for %s +++", gbpusd.getInstrument()));
        for (Order<String, String> order : pendingOrdersGbpUsd) {
            LOG.info(String.format("units=%s, takeprofit=%2.5f,stoploss=%2.5f,limitprice=%2.5f,side=%s", order.getUnits(), order.getTakeProfit(), order.getStopLoss(), order.getPrice(), order.getSide()));
        }

        int usdPosCt = orderInfoService.findNetPositionCountForCurrency("USD");
        int gbpPosCt = orderInfoService.findNetPositionCountForCurrency("GBP");
        LOG.info("Net Position count for USD = " + usdPosCt);
        LOG.info("Net Position count for GBP = " + gbpPosCt);
        Collection<Order<String, String>> pendingOrders = orderInfoService.allPendingOrders();
        LOG.info("+++++++++++++++++++ Dumping all pending orders ++++++++");
        for (Order<String, String> order : pendingOrders) {
            LOG.info(String.format("instrument=%s,units=%s, takeprofit=%2.5f,stoploss=%2.5f,limitprice=%2.5f,side=%s", order.getInstrument().getInstrument(), order.getUnits(), order.getTakeProfit(), order.getStopLoss(),
                    order.getPrice(), order.getSide()));
        }
    }

    private static void usage(String[] args) {
        if (args.length != 3) {
            LOG.error("Usage: OrderExecutionServiceDemo <url> <username> <accesstoken>");
            System.exit(1);
        }
    }
}
