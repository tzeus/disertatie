package test;

import java.util.Collection;

import org.apache.log4j.Logger;

import oandaAPI.account.OandaAccountDataProviderService;
import oandaAPI.order.OandaOrderManagementProvider;
import tradingAPI.account.AccountDataProvider;
import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.order.Order;
import tradingAPI.order.OrderInfoService;
import tradingAPI.order.OrderManagementProvider;

public class OrderInfoServiceTest {

	private static final Logger LOG = Logger.getLogger(OrderInfoServiceTest.class);

	private static void usage(String[] args) {
		if (args.length != 3) {
			LOG.error("Usage: OrderExecutionServiceDemo <url> <username> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) {

		usage(args);
		String url = args[0];
		String userName = args[1];
		String accessToken = args[2];

		AccountDataProvider<String> accountDataProvider = new OandaAccountDataProviderService(url, userName, accessToken);

		OrderManagementProvider<String, String, String> orderManagementProvider = new OandaOrderManagementProvider(url,
				accessToken, accountDataProvider);

		OrderInfoService<String, String, String> orderInfoService = new OrderInfoService<String, String, String>(
				orderManagementProvider);

		TradeableInstrument<String> gbpusd = new TradeableInstrument<String>("GBP_USD");

		orderInfoService.allPendingOrders();
		Collection<Order<String, String>> pendingOrdersGbpUsd = orderInfoService.pendingOrdersForInstrument(gbpusd);

		LOG.info(String.format("+++++++++++++++++++ Dumping all pending orders for %s +++", gbpusd.getInstrument()));
		for (Order<String, String> order : pendingOrdersGbpUsd) {
			LOG.info(String.format("units=%s, takeprofit=%2.5f,stoploss=%2.5f,limitprice=%2.5f,side=%s", order
					.getUnits(), order.getTakeProfit(), order.getStopLoss(), order.getPrice(), order.getSide()));
		}

		int usdPosCt = orderInfoService.findNetPositionCountForCurrency("USD");
		int gbpPosCt = orderInfoService.findNetPositionCountForCurrency("GBP");
		LOG.info("Net Position count for USD = " + usdPosCt);
		LOG.info("Net Position count for GBP = " + gbpPosCt);
		Collection<Order<String, String>> pendingOrders = orderInfoService.allPendingOrders();
		LOG.info("+++++++++++++++++++ Dumping all pending orders ++++++++");
		for (Order<String, String> order : pendingOrders) {
			LOG.info(String.format("instrument=%s,units=%s, takeprofit=%2.5f,stoploss=%2.5f,limitprice=%2.5f,side=%s",
					order.getInstrument().getInstrument(), order.getUnits(), order.getTakeProfit(),
					order.getStopLoss(), order.getPrice(), order.getSide()));
		}
	}
}
