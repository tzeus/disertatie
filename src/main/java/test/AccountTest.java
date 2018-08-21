package test;

import java.util.Collection;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import brokerAPI.account.BrokerAccountDataProviderService;
import brokerAPI.account.BrokerProviderHelper;
import brokerAPI.market.BrokerCurrentPriceInfoProvider;
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
		String userName = "toprisan";
		String accessToken = "5f65b265e3e232fa9cdef534bc112ad3-34841ec230e5b49d758499affb6b41e7";

		// initialise the dependencies
		AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, userName, accessToken);
		CurrentPriceInfoProvider<String, String> currentPriceInfoProvider = new BrokerCurrentPriceInfoProvider(url, accessToken);
		BaseTradingConfig tradingConfig = new BaseTradingConfig();
		tradingConfig.setMinReserveRatio(0.05);
		tradingConfig.setMinAmountRequired(100.00);
		ProviderHelper<String> providerHelper = new BrokerProviderHelper();

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
		TradeableInstrument<String> eurchf = new TradeableInstrument<String>("EUR_CHF");
		double gbpusdMarginReqd = accountInfoService.calculateMarginForTrade(sampleAccount, gbpusd, units);
		System.out.println("After GBP_USD...........................................................");
		double eurgbpMarginReqd = accountInfoService.calculateMarginForTrade(sampleAccount, eurchf, units);
		System.out.println("After EUR_CHF............................................................");
		LOG.info(String.format("Marging requirement for trading pair %d units of %s is %5.2f %s ", units, gbpusd
				.getInstrument(), gbpusdMarginReqd, sampleAccount.getCurrency()));
		LOG.info(String.format("Marging requirement for trading pair %d units of %s is %5.2f %s ", units, eurchf
				.getInstrument(), eurgbpMarginReqd, sampleAccount.getCurrency()));
	}

}
