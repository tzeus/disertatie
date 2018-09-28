/**
 * Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * <p>
 * This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 * express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.tudoreloprisan.controller;

import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.tudoreloprisan.brokerAPI.market.BrokerCurrentPriceInfoProvider;
import com.tudoreloprisan.brokerAPI.marketData.BrokerHistoricMarketDataProvider;
import com.tudoreloprisan.repositories.HistoricalData;
import com.tudoreloprisan.repositories.HistoricalDataRepository;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.market.Price;
import com.tudoreloprisan.tradingAPI.marketData.CandleStick;
import com.tudoreloprisan.tradingAPI.marketData.CandleStickGranularity;
import com.tudoreloprisan.tradingAPI.marketData.HistoricMarketDataProvider;

import com.tudoreloprisan.util.DateTimeUtil;
import org.apache.log4j.Logger;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;


@RestController
@PropertySource("classpath:auth.properties")
public class PriceController {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(PriceController.class);

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
    @Autowired
    private HistoricalDataRepository historicalDataRepository;

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

    @RequestMapping(value = "/getHistData", method = RequestMethod.GET)
    public String getHistoricData(@RequestParam(value = "instrument") String instrument,
                                  @RequestParam(value = "granularity") String granularity,
                                  @RequestParam(value = "amount") String amount) {
        HistoricMarketDataProvider<String> historicMarketDataProvider = new BrokerHistoricMarketDataProvider(url, accessToken);
        TradeableInstrument<String> usdchf = new TradeableInstrument<String>(instrument);
        List<CandleStick<String>> candlesForInstrument = historicMarketDataProvider.getCandleSticks(usdchf, CandleStickGranularity.valueOf(granularity), Integer.parseInt(amount));

        for (CandleStick<String> candle : candlesForInstrument) {
            LOG.info(candle);
        }

        //TODO -> Output for Front-End
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        JsonArray asJsonArray = new GsonBuilder().disableHtmlEscaping().create().toJsonTree(candlesForInstrument).getAsJsonArray();

        for (int i = 0; i < asJsonArray.size(); i++) {

            String dateAsString = new ArrayList<CandleStick>(candlesForInstrument).get(i).getEventDate().toString(DateTimeFormat.forPattern("EEE MMM dd yyyy HH:mm:ss Z' ('z')'"));
            asJsonArray.get(i).getAsJsonObject().addProperty("eventDate", dateAsString);
            asJsonArray.get(i).getAsJsonObject().remove("instrument");
            asJsonArray.get(i).getAsJsonObject().remove("candleGranularity");
            asJsonArray.get(i).getAsJsonObject().remove("hash");
            asJsonArray.get(i).getAsJsonObject().remove("toStr");

        }

        JsonObject orders2 = new JsonObject();
        orders2.add("allOrders", asJsonArray);
        try (FileWriter file = new FileWriter("/frontend/src/assets/orders2.json")) {

            file.write(gson.toJson(orders2));
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
//        String tsvDataFromCandlestickDate = getTsvDataFromCandlestickDate(asJsonArray, instrument, granularity, amount);
//         String candlestickData = gson.toJson(candlesForInstrument);

        return gson.toJson(asJsonArray);

    }

    @RequestMapping(value = "/getHistoricalData", method = RequestMethod.GET)
    public String getHistoricalData(@RequestParam(value = "instrument") String instrument,
                                    @RequestParam(value = "granularity") String granularity,
                                    @RequestParam(value = "amount") String amount) {
        HistoricMarketDataProvider<String> historicMarketDataProvider = new BrokerHistoricMarketDataProvider(url, accessToken);
        TradeableInstrument<String> usdchf = new TradeableInstrument<String>(instrument);
        List<HistoricalData> historicalDataForInstrument = historicMarketDataProvider.getHistoricalDataForInstrument(usdchf, CandleStickGranularity.valueOf(granularity), Integer.parseInt(amount));

        historicalDataRepository.saveAll(historicalDataForInstrument);
        for (HistoricalData data : historicalDataForInstrument) {
            LOG.info(data);
        }

        //TODO -> Output for Front-End
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        JsonArray asJsonArray = new GsonBuilder().disableHtmlEscaping().create().toJsonTree(historicalDataForInstrument).getAsJsonArray();

//        for (int i = 0; i < asJsonArray.size(); i++) {
//
//            String dateAsString = new ArrayList<>(historicalDataForInstrument).get(i).getTimestamp().toString(DateTimeFormat.forPattern("EEE MMM dd yyyy HH:mm:ss Z' ('z')'"));
//            asJsonArray.get(i).getAsJsonObject().addProperty("eventDate", dateAsString);
//            asJsonArray.get(i).getAsJsonObject().remove("instrument");
//            asJsonArray.get(i).getAsJsonObject().remove("candleGranularity");
//            asJsonArray.get(i).getAsJsonObject().remove("hash");
//            asJsonArray.get(i).getAsJsonObject().remove("toStr");
//
//        }

        return gson.toJson(asJsonArray);

    }

    @RequestMapping(value = "/getHistoricalDataBetweenDates", method = RequestMethod.GET)
    public String getHistoricalDataBetweenDates(@RequestParam(value = "instrument") String instrument,
                                                @RequestParam(value = "granularity") String granularity,
                                                @RequestParam(value = "startDate") String startDate,
                                                @RequestParam(value = "endDate") String endDate) {
        BrokerHistoricMarketDataProvider historicMarketDataProvider = new BrokerHistoricMarketDataProvider(url, accessToken);
        TradeableInstrument<String> usdchf = new TradeableInstrument<>(instrument);
        List<HistoricalData> historicalDataForInstrumentMonthly = new ArrayList<>();

        DateTime startDateTimeFromRegularDateTime = DateTimeUtil.getDateTimeFromRegularDateTime(startDate);
        DateTime endDateTimeFromRegularDateTime = DateTimeUtil.getDateTimeFromRegularDateTime(endDate);

        for (int i = 0; i < 30; i++) {
            startDateTimeFromRegularDateTime = startDateTimeFromRegularDateTime.plusDays(1);
            endDateTimeFromRegularDateTime = endDateTimeFromRegularDateTime.plusDays(1);


            String startDateAsString = DateTimeUtil.getDateTimeAsStringFromRegularDateTime(startDateTimeFromRegularDateTime.toString());
            String endDateAsString = DateTimeUtil.getDateTimeAsStringFromRegularDateTime(endDateTimeFromRegularDateTime.toString());

            historicalDataForInstrumentMonthly.addAll(historicMarketDataProvider.getHistoricalDataForInstrumentBetweenDates(usdchf, CandleStickGranularity.valueOf(granularity), startDateAsString, endDateAsString));
        }


        historicalDataRepository.saveAll(historicalDataForInstrumentMonthly);
//        for (HistoricalData data : historicalDataForInstrument) {
//            LOG.info(data);
//        }

        //TODO -> Output for Front-End
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        JsonArray asJsonArray = new GsonBuilder().disableHtmlEscaping().create().toJsonTree(historicalDataForInstrumentMonthly).getAsJsonArray();

        return gson.toJson(asJsonArray);

    }
}
