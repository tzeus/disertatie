package test;

import java.util.Collection;

import org.apache.log4j.Logger;

import brokerAPI.account.BrokerAccountDataProviderService;
import brokerAPI.trade.BrokerTradeManagementProvider;
import tradingAPI.account.AccountDataProvider;
import tradingAPI.account.BaseTradingConfig;
import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.trade.Trade;
import tradingAPI.trade.TradeInfoService;
import tradingAPI.trade.TradeManagementProvider;

public class TradeInfoServiceTest {

	private static final Logger LOG = Logger.getLogger(TradeInfoServiceTest.class);

	private static void usage(String[] args) {
		if (args.length != 3) {
			LOG.error("Usage: TradeInfoServiceDemo <url> <username> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		usage(args);
		String url = args[0];
		String userName = args[1];
		String accessToken = args[2];
		AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, userName, accessToken);
		BaseTradingConfig tradingConfig = new BaseTradingConfig();
		tradingConfig.setMinReserveRatio(0.05);
		tradingConfig.setMinAmountRequired(100.00);
		tradingConfig.setMaxAllowedQuantity(10);
		TradeManagementProvider<String, String, String> tradeManagementProvider = new BrokerTradeManagementProvider(url,
				accessToken);
		TradeInfoService<String, String, String> tradeInfoService = new TradeInfoService<String, String, String>(
				tradeManagementProvider, accountDataProvider);

		tradeInfoService.init();
		Collection<Trade<String, String, String>> allTrades = tradeInfoService.getAllTrades();
		LOG.info("################ Dumping All Trades ################");
		for (Trade<String, String, String> trade : allTrades) {
			LOG.info(String.format("Units=%s,Side=%s,Instrument=%s,Price=%2.5f", trade.getUnits(), trade.getSide(),
					trade.getInstrument().getInstrument(), trade.getExecutionPrice()));
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

}
