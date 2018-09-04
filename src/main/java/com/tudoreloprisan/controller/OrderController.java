/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.tudoreloprisan.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.tudoreloprisan.brokerAPI.account.BrokerAccountDataProviderService;
import com.tudoreloprisan.brokerAPI.account.BrokerJsonKeys;
import com.tudoreloprisan.brokerAPI.account.BrokerProviderHelper;
import com.tudoreloprisan.brokerAPI.events.OrderEvents;
import com.tudoreloprisan.brokerAPI.market.BrokerCurrentPriceInfoProvider;
import com.tudoreloprisan.brokerAPI.marketData.BrokerHistoricMarketDataProvider;
import com.tudoreloprisan.brokerAPI.order.BrokerOrderManagementProvider;
import com.tudoreloprisan.brokerAPI.trade.BrokerTradeManagementProvider;
import com.tudoreloprisan.tradingAPI.account.AccountDataProvider;
import com.tudoreloprisan.tradingAPI.account.AccountInfoService;
import com.tudoreloprisan.tradingAPI.account.BaseTradingConfig;
import com.tudoreloprisan.tradingAPI.account.ProviderHelper;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.market.CurrentPriceInfoProvider;
import com.tudoreloprisan.tradingAPI.marketData.CandleStick;
import com.tudoreloprisan.tradingAPI.marketData.CandleStickGranularity;
import com.tudoreloprisan.tradingAPI.marketData.HistoricMarketDataProvider;
import com.tudoreloprisan.tradingAPI.marketData.MovingAverageCalculationService;
import com.tudoreloprisan.tradingAPI.order.*;
import com.tudoreloprisan.tradingAPI.trade.*;

import org.apache.log4j.Logger;

import org.joda.time.format.DateTimeFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;


@RestController
@PropertySource("classpath:auth.properties")
public class OrderController {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(OrderController.class);

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

    private AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);
    OrderManagementProvider<String, String, String> orderManagementProvider = new BrokerOrderManagementProvider(url, accessToken, accountDataProvider);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

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

    @RequestMapping(value = "/getOrders", method = RequestMethod.GET)
    @CrossOrigin(origins = "*") //TODO CHANGE ME
    public String getOrders() {
        OrderManagementProvider<String, String, String> orderManagementProvider = new BrokerOrderManagementProvider(url, accessToken, accountDataProvider);

        OrderInfoService<String, String, String> orderInfoService = new OrderInfoService<String, String, String>(orderManagementProvider);

        Collection<Order<String, String>> orders = orderInfoService.getAllOrders(accountId);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        JsonArray jsonArray = gson.toJsonTree(orders).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
//            String dateAsString = new ArrayList<Order>(orders).get(i).getCreateTime().toString();
//            jsonArray.get(i).getAsJsonObject().addProperty("createTime", dateAsString.substring(0, dateAsString.lastIndexOf('.')).replace('T', ' '));
//            jsonArray.get(i).getAsJsonObject().addProperty("instrument", new ArrayList<Order>(orders).get(i).getInstrument().getInstrument());
            String dateAsString = new ArrayList<Order>(orders).get(i).getCreateTime().toString(DateTimeFormat.forPattern("EEE MMM dd yyyy HH:mm:ss Z' ('z')'"));
            jsonArray.get(i).getAsJsonObject().addProperty("createTime", dateAsString);
        }

//        JsonObject orders2 = new JsonObject();
//        orders2.add("allOrders", jsonArray);
//        try (FileWriter file = new FileWriter("frontend/src/assets/orders2.json")) {
//
//            file.write(gson.toJson(orders2));
//            file.flush();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return gson.toJson(jsonArray);
    }

    @RequestMapping(value = "/getPendingOrders", method = RequestMethod.GET)
    public String getPendingOrders() {
        AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);

        BrokerOrderManagementProvider orderManagementProvider = new BrokerOrderManagementProvider(url, accessToken, accountDataProvider);
        OrderInfoService<String, String, String> orderInfoService = new OrderInfoService<String, String, String>(orderManagementProvider);
        Collection<Order<String, String>> orders = orderInfoService.allPendingOrders();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        JsonArray jsonArray = gson.toJsonTree(orders).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonArray.get(i).getAsJsonObject().addProperty("createTime", new ArrayList<Order>(orders).get(i).getCreateTime().toString());
        }

        return gson.toJson(orders);
    }

    @RequestMapping(value = "/closeTrade", method = RequestMethod.PUT)
    public String closeTrade(@RequestParam(value = "tradeId") String tradeId) {
        BrokerTradeManagementProvider brokerTradeManagementProvider = new BrokerTradeManagementProvider(url, accessToken);
        boolean result = brokerTradeManagementProvider.closeTrade(tradeId, accountId);

        return Boolean.toString(result);
    }

    @RequestMapping(value = "/getTrade", method = RequestMethod.GET)
    public String getTrade(@RequestParam(value = "tradeId") String tradeId) {
        BrokerTradeManagementProvider brokerTradeManagementProvider = new BrokerTradeManagementProvider(url, accessToken);
        Object tradeForAccount = brokerTradeManagementProvider.getTradeForAccount(tradeId, accountId);
        Gson gson = new Gson();
        JsonObject jsonObject;
        if (tradeForAccount instanceof Trade) {
            Trade trade = (Trade) tradeForAccount;
            String tradeJson = gson.toJson(tradeForAccount);
            jsonObject = gson.fromJson(tradeJson, JsonObject.class);
            jsonObject.addProperty("tradeDate", trade.getTradeDate().toString());
        } else {
            jsonObject = gson.fromJson((String) tradeForAccount, JsonObject.class);
        }
        return gson.toJson(jsonObject);
    }

    @RequestMapping(value = "/getTrades", method = RequestMethod.GET)
    @CrossOrigin(origins = "*") //TODO CHANGE ME
    public String getTrades() {
        BrokerTradeManagementProvider brokerTradeManagementProvider = new BrokerTradeManagementProvider(url, accessToken);
        Collection<Trade<String, String, String>> tradesForAccount = brokerTradeManagementProvider.getTradesForAccount(accountId);
        String trades = new GsonBuilder().disableHtmlEscaping().create().toJson(tradesForAccount);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        JsonArray jsonArray = gson.toJsonTree(tradesForAccount).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonArray.get(i).getAsJsonObject().addProperty("tradeDate", new ArrayList<Trade>(tradesForAccount).get(i).getTradeDate().toString());
        }
        String tradeJson = gson.toJson(trades);

//        JsonObject trades2 = new JsonObject();
//        trades2.add("allTrades", jsonArray);
//        try (FileWriter file = new FileWriter("frontend/src/assets/trades2.json")) {
//
//            file.write(gson.toJson(trades2));
//            file.flush();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return gson.toJson(jsonArray);

    }

    @RequestMapping(value = "/getOpenTrades", method = RequestMethod.GET)
    public String getOpenTrades() {
        BrokerTradeManagementProvider brokerTradeManagementProvider = new BrokerTradeManagementProvider(url, accessToken);
        Collection<Trade<String, String, String>> tradesForAccount = brokerTradeManagementProvider.getOpenTradesForAccount(accountId);
        String trades = new GsonBuilder().disableHtmlEscaping().create().toJson(tradesForAccount);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        JsonArray jsonArray = gson.toJsonTree(tradesForAccount).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonArray.get(i).getAsJsonObject().addProperty("tradeDate", new ArrayList<Trade>(tradesForAccount).get(i).getTradeDate().toString());
        }
        String tradeJson = gson.toJson(trades);

        return gson.toJson(jsonArray);

    }

    @RequestMapping(value = "/transactions", method = RequestMethod.GET)
    public String getTransactions(@RequestParam(value = "sinceId", defaultValue = "1") String sinceId) {
        AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);
        BrokerOrderManagementProvider orderManagementProvider = new BrokerOrderManagementProvider(url, accessToken, accountDataProvider);
        String allTransactions = orderManagementProvider.getAllTransactions(accountId, sinceId);
        return allTransactions;
    }

    @RequestMapping(value = "/deleteOrder", method = RequestMethod.PUT)
    public String deleteOrder(@RequestParam(value = "orderId") String orderId) {
        AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);
        OrderManagementProvider<String, String, String> orderManagementProvider = new BrokerOrderManagementProvider(url, accessToken, accountDataProvider);
        boolean cancelOrderResponse = orderManagementProvider.closeOrder(orderId, accountId);
        return cancelOrderResponse ? "Order cancelled" : "Error";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getOrder", method = RequestMethod.GET)
    public String getOrderInfo(@RequestParam(value = "orderId") String orderId) {
        AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);
        BrokerOrderManagementProvider orderManagementProvider = new BrokerOrderManagementProvider(url, accessToken, accountDataProvider);
        Object order = orderManagementProvider.pendingOrderForAccount(orderId, accountId);
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(order);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/placeMarketOrder", method = RequestMethod.POST)
    public String placeMarketOrder(@RequestParam(value = "units") String units,
        @RequestParam(value = "timeInForce") String timeInForce,
        @RequestParam(value = "instrument") String instrument) throws InterruptedException {

        AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);
        BaseTradingConfig tradingConfig = new BaseTradingConfig();
        tradingConfig.setMinReserveRatio(0.05);
        tradingConfig.setMinAmountRequired(100.00);
        tradingConfig.setMaxAllowedQuantity(10);

        OrderManagementProvider<String, String, String> orderManagementProvider = new BrokerOrderManagementProvider(url, accessToken, accountDataProvider);

        int unit = Integer.parseInt(units);
        Order order = new Order(new TradeableInstrument<String>(instrument), units.replace("-", ""), (unit < 0) ? TradingSignal.SHORT : TradingSignal.LONG, OrderType.MARKET);
        String orderId = orderManagementProvider.placeOrder(order, accountId);

        return orderId;

    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/placeLimitOrder", method = RequestMethod.POST)
    public String placeMarketOrder(@RequestParam(value = "units") String units,
        @RequestParam(value = "timeInForce") String timeInForce,
        @RequestParam(value = "stopLoss") String stopLoss,
        @RequestParam(value = "price") String price,
        @RequestParam(value = "takeProfit") String takeProfit,
        @RequestParam(value = "instrument") String instrument) throws InterruptedException {

        AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);
        OrderManagementProvider<String, String, String> orderManagementProvider = new BrokerOrderManagementProvider(url, accessToken, accountDataProvider);

        int unit = Integer.parseInt(units);

        double tp = Double.parseDouble(takeProfit);
        double sl = Double.parseDouble(stopLoss);
        double pr = Double.parseDouble(price);
        Order order = new Order(new TradeableInstrument<String>(instrument), units.replace("-", ""), (unit < 0) ? TradingSignal.SHORT : TradingSignal.LONG, OrderType.LIMIT,
            BigDecimal.valueOf(tp).setScale(4, RoundingMode.HALF_UP).doubleValue(), BigDecimal.valueOf(sl).setScale(4, RoundingMode.HALF_UP).doubleValue(),
            BigDecimal.valueOf(pr).setScale(4, RoundingMode.HALF_UP).doubleValue());
        String orderId = orderManagementProvider.placeOrder(order, accountId);

        return orderId;

    }

    @RequestMapping(value = "/placeOrder", method = RequestMethod.POST)
    public String placeOrder(@RequestParam(value = "units") String units,
        @RequestParam(value = "instrument") String instrument,
        @RequestParam(value = "timeInForce") String timeInForce,
        @RequestParam(value = "positionFill") String positionFill,
        @RequestParam(value = "entryPoint") String entryPoint,
        @RequestParam(value = "direction") String direction,
        @RequestParam(value = "stopLossOnFill") String stopLossOnFill,
        @RequestParam(value = "orderType") String orderType,
        @RequestParam(value = "takeProfitOnFill") String takeProfitOnFill) throws InterruptedException {

        BlockingQueue<TradingDecision<String>> orderQueue = new LinkedBlockingQueue<TradingDecision<String>>();

        AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);
        CurrentPriceInfoProvider<String, String> currentPriceInfoProvider = new BrokerCurrentPriceInfoProvider(url, accessToken);
        BaseTradingConfig tradingConfig = new BaseTradingConfig();
        tradingConfig.setMinReserveRatio(0.05);
        tradingConfig.setMinAmountRequired(100.00);
        tradingConfig.setMaxAllowedQuantity(10);
        ProviderHelper<String> providerHelper = new BrokerProviderHelper();
        AccountInfoService<String, String> accountInfoService = new AccountInfoService<String, String>(accountDataProvider, currentPriceInfoProvider, tradingConfig, providerHelper);

        OrderManagementProvider<String, String, String> orderManagementProvider = new BrokerOrderManagementProvider(url, accessToken, accountDataProvider);

        TradeManagementProvider<String, String, String> tradeManagementProvider = new BrokerTradeManagementProvider(url, accessToken);

        OrderInfoService<String, String, String> orderInfoService = new OrderInfoService<String, String, String>(orderManagementProvider);

        TradeInfoService<String, String, String> tradeInfoService = new TradeInfoService<String, String, String>(tradeManagementProvider, accountDataProvider);

        HistoricMarketDataProvider<String> historicMarketDataProvider = new BrokerHistoricMarketDataProvider(url, accessToken);

        MovingAverageCalculationService<String> movingAverageCalculationService = new MovingAverageCalculationService<String>(historicMarketDataProvider);

        PreOrderValidationService<String, String, String> preOrderValidationService = new PreOrderValidationService<String, String, String>(tradeInfoService, movingAverageCalculationService, tradingConfig, orderInfoService);

        OrderExecutionService<String, String, String> orderExecService = new OrderExecutionService<String, String, String>(orderQueue, accountInfoService, orderManagementProvider, tradingConfig, preOrderValidationService,
            currentPriceInfoProvider);
        orderExecService.init();
        TradingDecision<String> decision = null;
        switch (OrderEvents.valueOf(orderType)) {

        case MARKET_ORDER:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        case LIMIT_ORDER:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        case TAKE_PROFIT_ORDER:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        case STOP_LOSS_ORDER:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        case MARKET_IF_TOUCHED_ORDER:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        case ORDER_FILL:
            decision = new TradingDecision<String>(new TradeableInstrument<String>(instrument), TradingSignal.valueOf(direction), Double.parseDouble(takeProfitOnFill), Double.parseDouble(stopLossOnFill),
                Double.parseDouble(entryPoint));
            break;

        default:
            break;
        }
        orderQueue.offer(decision);
        Thread.sleep(10000); // enough time to place an order
        orderExecService.shutDown();

        return "";

    }

    private String getTsvDataFromCandlestickDate(JsonArray candlestickData, String instrument, String granularity, String amount) {
        StringBuilder tsvData = new StringBuilder();

        //FileWriter fos = null;
        try {
            File file = new File("D:/Work/0/disertatie/data/candles-for-" + instrument + '-' + granularity + '-' + amount + ".tsv");
            file.getParentFile().mkdirs();
            file.createNewFile();
            PrintWriter dos = new PrintWriter(file);
            //    fos = new FileWriter(file);
//            PrintWriter dos = new PrintWriter(fos);
// loop through all your data and print it to the file
            final ArrayList<Boolean> isFirstLine = new ArrayList<>();
            isFirstLine.add(true);

            dos.println("date,open,close,");
            tsvData.append("date,open,close,").append(System.lineSeparator());
            candlestickData.forEach(jsonElement -> {
                JsonObject candleStickJsonObject = jsonElement.getAsJsonObject();
                String openPrice = candleStickJsonObject.get("openPrice").getAsString();
//                String highPrice = candleStickJsonObject.get("highPrice").getAsString();
//                String lowPrice = candleStickJsonObject.get("lowPrice").getAsString();
                String closePrice = candleStickJsonObject.get("closePrice").getAsString();
//                String candleGranularity = candleStickJsonObject.get("candleGranularity").getAsString();
                JsonObject chronology = candleStickJsonObject.get("eventDate").getAsJsonObject();
                String time = chronology.get("iMillis").getAsString();
//                String volume = candleStickJsonObject.get("volume").getAsString();
                //logic here
                dos.print(time + ",");
                dos.print(openPrice + ",");
//                dos.print(highPrice + "\t");
//                dos.print(lowPrice + "\t");
                dos.print(closePrice + ",");
//                dos.print(volume + "\t");
                tsvData.append(time).append(',').append(openPrice).append(',').append(closePrice).append(System.lineSeparator());
                dos.println();
                isFirstLine.add(0, false);
            });
            dos.close();

        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return tsvData.toString();
    }

}
