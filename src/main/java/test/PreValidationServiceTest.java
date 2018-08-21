package test;

import org.apache.log4j.Logger;

import brokerAPI.account.BrokerAccountDataProviderService;
import brokerAPI.marketData.BrokerHistoricMarketDataProvider;
import brokerAPI.order.BrokerOrderManagementProvider;
import brokerAPI.trade.BrokerTradeManagementProvider;
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
	

		private static final Logger LOG = Logger.getLogger(PreValidationServiceTest.class);

		private static void usage(String[] args) {
			if (args.length != 3) {
				LOG.error("Usage: PreValidationServiceDemo <url> <username> <accesstoken>");
				System.exit(1);
			}
		}

		public static void main(String[] args) {
			usage(args);
			String url = args[0];
			String userName = args[1];
			String accessToken = args[2];

			AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, userName,
					accessToken);

			OrderManagementProvider<String, String, String> orderManagementProvider = new BrokerOrderManagementProvider(url,
					accessToken, accountDataProvider);

			TradeManagementProvider<String, String, String> tradeManagementProvider = new BrokerTradeManagementProvider(url,
					accessToken);

			BaseTradingConfig tradingConfig = new BaseTradingConfig();
			tradingConfig.setMinReserveRatio(0.05);
			tradingConfig.setMinAmountRequired(100.00);
			tradingConfig.setMaxAllowedQuantity(10);
			tradingConfig.setMaxAllowedNetContracts(3);

			TradeInfoService<String, String, String> tradeInfoService = new TradeInfoService<String, String, String>(
					tradeManagementProvider, accountDataProvider);

			tradeInfoService.init();

			HistoricMarketDataProvider<String> historicMarketDataProvider = new BrokerHistoricMarketDataProvider(url,
					accessToken);

			MovingAverageCalculationService<String> movingAverageCalculationService = new MovingAverageCalculationService<String>(
					historicMarketDataProvider);

			OrderInfoService<String, String, String> orderInfoService = new OrderInfoService<String, String, String>(
					orderManagementProvider);

			PreOrderValidationService<String, String, String> preOrderValidationService = new PreOrderValidationService<String, String, String>(
					tradeInfoService, movingAverageCalculationService, tradingConfig, orderInfoService);

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

	}
