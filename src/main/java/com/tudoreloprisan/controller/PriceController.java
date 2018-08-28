/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.tudoreloprisan.controller;

import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tudoreloprisan.brokerAPI.market.BrokerCurrentPriceInfoProvider;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.market.Price;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;


@RestController
@PropertySource("classpath:auth.properties")
public class PriceController {

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

    @RequestMapping(value = "/getPrice", method = RequestMethod.GET)
    @CrossOrigin(origins = "*") //TODO CHANGE ME
    public String getPriceForInstrument(@RequestParam(value = "currencyPair") String currencyPair) {
        BrokerCurrentPriceInfoProvider priceProvider = new BrokerCurrentPriceInfoProvider(url, accessToken);
        TradeableInstrument<String> instrument = new TradeableInstrument<>(currencyPair);
        ArrayList<TradeableInstrument<String>> collection = new ArrayList<>();
        collection.add(instrument);
        Map<TradeableInstrument<String>, Price<String>> currentPricesForInstruments = priceProvider.getCurrentPricesForInstruments(collection, accountId);

        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        return gson.toJson(currentPricesForInstruments);
    }
}
