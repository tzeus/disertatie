package test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import oandaAPI.account.OandaAccountDataProviderService;
import oandaAPI.account.OandaProviderHelper;
import oandaAPI.market.OandaCurrentPriceInfoProvider;
import oandaAPI.marketData.OandaHistoricMarketDataProvider;
import oandaAPI.order.OandaOrderManagementProvider;
import oandaAPI.trade.OandaTradeManagementProvider;
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

public class OrderExecutionServiceTest {

	private static final Logger LOG = Logger.getLogger(OrderExecutionServiceTest.class);

	private static void usage(String[] args) {
		if (args.length != 3) {
			LOG.error("Usage: OrderExecutionServiceDemo <url> <username> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {

		usage(args);
		String url = args[0];
		String userName = args[1];
		String accessToken = args[2];

		BlockingQueue<TradingDecision<String>> orderQueue = new LinkedBlockingQueue<TradingDecision<String>>();

		AccountDataProvider<String> accountDataProvider = new OandaAccountDataProviderService(url, userName, accessToken);
		CurrentPriceInfoProvider<String, String> currentPriceInfoProvider = new OandaCurrentPriceInfoProvider(url, accessToken);
		BaseTradingConfig tradingConfig = new BaseTradingConfig();
		tradingConfig.setMinReserveRatio(0.05);
		tradingConfig.setMinAmountRequired(100.00);
		tradingConfig.setMaxAllowedQuantity(10);
		ProviderHelper<String> providerHelper = new OandaProviderHelper();
		AccountInfoService<String, String> accountInfoService = new AccountInfoService<String, String>(accountDataProvider,
				currentPriceInfoProvider, tradingConfig, providerHelper);

		OrderManagementProvider<String, String, String> orderManagementProvider = new OandaOrderManagementProvider(url,
				accessToken, accountDataProvider);

		TradeManagementProvider<String, String, String> tradeManagementProvider = new OandaTradeManagementProvider(url,
				accessToken);

		OrderInfoService<String, String, String> orderInfoService = new OrderInfoService<String, String, String>(
				orderManagementProvider);

		TradeInfoService<String, String, String> tradeInfoService = new TradeInfoService<String, String, String>(
				tradeManagementProvider, accountDataProvider);

		HistoricMarketDataProvider<String> historicMarketDataProvider = new OandaHistoricMarketDataProvider(url,
				accessToken);

		MovingAverageCalculationService<String> movingAverageCalculationService = new MovingAverageCalculationService<String>(
				historicMarketDataProvider);

		PreOrderValidationService<String, String, String> preOrderValidationService = new PreOrderValidationService<String, String, String>(
				tradeInfoService, movingAverageCalculationService, tradingConfig, orderInfoService);

		OrderExecutionService<String, String, String> orderExecService = new OrderExecutionService<String, String, String>(
				orderQueue, accountInfoService, orderManagementProvider, tradingConfig, preOrderValidationService,
				currentPriceInfoProvider);
		orderExecService.init();

		TradingDecision<String> decision = new TradingDecision<String>(new TradeableInstrument<String>("GBP_USD"),
				TradingSignal.LONG, 1.44, 1.35, 1.4);
		orderQueue.offer(decision);
		Thread.sleep(10000);// enough time to place an order
		orderExecService.shutDown();

	}
}
