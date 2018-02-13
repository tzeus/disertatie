package test;

import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import oandaAPI.account.OandaAccountDataProviderService;
import oandaAPI.account.OandaProviderHelper;
import oandaAPI.market.OandaCurrentPriceInfoProvider;
import tradingAPI.account.Account;
import tradingAPI.account.AccountDataProvider;
import tradingAPI.account.AccountInfoService;
import tradingAPI.account.BaseTradingConfig;
import tradingAPI.account.ProviderHelper;
import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.market.CurrentPriceInfoProvider;

public class AccountTest {
	private static final Logger LOG = Logger.getLogger(AccountTest.class);

	

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String url = "https://api-fxpractice.oanda.com";
		String userName = "bagrov";
		String accessToken = "ad636147722167721aab6cf1550624ff-9d14ce6efe8fa5f1bff7d976ae492673";

		// initialise the dependencies
		AccountDataProvider<String> accountDataProvider = new OandaAccountDataProviderService(url, userName, accessToken);
		CurrentPriceInfoProvider<String, String> currentPriceInfoProvider = new OandaCurrentPriceInfoProvider(url, accessToken);
		BaseTradingConfig tradingConfig = new BaseTradingConfig();
		tradingConfig.setMinReserveRatio(0.05);
		tradingConfig.setMinAmountRequired(100.00);
		ProviderHelper<String> providerHelper = new OandaProviderHelper();

		AccountInfoService<String, String> accountInfoService = new AccountInfoService<String, String>(accountDataProvider,
				currentPriceInfoProvider, tradingConfig, providerHelper);

		Collection<Account<String>> accounts = accountInfoService.getAllAccounts();
		LOG.info(String.format("Found %d accounts to trade for user %s", accounts.size(), userName));
		LOG.info("+++++++++++++++++++++++++++++++ Dumping Account Info +++++++++++++++++++++++++++++");
		for (Account<String> account : accounts) {
			LOG.info(account);
		}
		LOG.info("++++++++++++++++++++++ Finished Dumping Account Info +++++++++++++++++++++++++++++");
		Account<String> sampleAccount = accounts.iterator().next();
		final int units = 5000;
		TradeableInstrument<String> gbpusd = new TradeableInstrument<String>("GBP_USD");
		TradeableInstrument<String> eurgbp = new TradeableInstrument<String>("EUR_GBP");
		double gbpusdMarginReqd = accountInfoService.calculateMarginForTrade(sampleAccount, gbpusd, units);
		System.out.println("After GBP_USD...........................................................");
		double eurgbpMarginReqd = accountInfoService.calculateMarginForTrade(sampleAccount, eurgbp, units);
		System.out.println("After EUR_GBP............................................................");
		LOG.info(String.format("Marging requirement for trading pair %d units of %s is %5.2f %s ", units, gbpusd
				.getInstrument(), gbpusdMarginReqd, sampleAccount.getCurrency()));
		LOG.info(String.format("Marging requirement for trading pair %d units of %s is %5.2f %s ", units, eurgbp
				.getInstrument(), eurgbpMarginReqd, sampleAccount.getCurrency()));
	}

}
