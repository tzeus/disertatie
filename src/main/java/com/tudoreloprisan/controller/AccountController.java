package com.tudoreloprisan.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tudoreloprisan.brokerAPI.account.BrokerAccountDataProviderService;
import com.tudoreloprisan.brokerAPI.account.BrokerProviderHelper;
import com.tudoreloprisan.brokerAPI.market.BrokerCurrentPriceInfoProvider;
import com.tudoreloprisan.tradingAPI.account.*;
import com.tudoreloprisan.tradingAPI.market.CurrentPriceInfoProvider;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PropertySource("classpath:auth.properties")
public class AccountController {

    @Autowired
    private Environment env;

    private static final Logger LOG = Logger.getLogger(AccountController.class);
    @Value("${broker.url}")
    private String url;//  = env.getProperty("broker.url");
    @Value("${broker.user}")
    private String user;// =env.getProperty("broker.user");
    @Value("${broker.accessToken}")
    private String accessToken;// =env.getProperty("broker.accessToken");
    @Value("${broker.accountId}")
    private String accoutdId;// =env.getProperty("broker.accountId");

    @RequestMapping(value = "/account", method = RequestMethod.GET)
    @CrossOrigin(origins = "*") //TODO CHANGEME
    public String getAccountDetails(){
        String returnedValue ="";

        AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);
        CurrentPriceInfoProvider<String, String> currentPriceInfoProvider = new BrokerCurrentPriceInfoProvider(url, accessToken);
        BaseTradingConfig tradingConfig = new BaseTradingConfig();
        tradingConfig.setMinReserveRatio(0.05);
        tradingConfig.setMinAmountRequired(100.00);
        ProviderHelper<String> providerHelper = new BrokerProviderHelper();

        AccountInfoService<String, String> accountInfoService = new AccountInfoService<String, String>(accountDataProvider,
                currentPriceInfoProvider, tradingConfig, providerHelper);

        Account<String> accountInfo = accountInfoService.getAccountInfo(accoutdId);

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(accountInfo);
    }
}

