package com.tudoreloprisan.controller;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.tudoreloprisan.brokerAPI.account.BrokerAccountDataProviderService;
import com.tudoreloprisan.brokerAPI.streaming.BrokerMarketDataStreamingService;
import com.tudoreloprisan.test.DefaultHeartBeatServiceTest;
import com.tudoreloprisan.test.MarketDataStreamingServiceTest;
import com.tudoreloprisan.tradingAPI.account.AccountDataProvider;
import com.tudoreloprisan.tradingAPI.heartbeat.DefaultHeartBeatService;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatCallback;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatCallbackImpl;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatPayLoad;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.marketData.MarketDataPayLoad;
import com.tudoreloprisan.tradingAPI.marketData.MarketEventCallback;
import com.tudoreloprisan.tradingAPI.marketData.MarketEventHandlerImpl;
import com.tudoreloprisan.tradingAPI.streaming.HeartBeatStreamingService;
import com.tudoreloprisan.tradingAPI.streaming.MarketDataStreamingService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@PropertySource("classpath:auth.properties")
public class StreamingServiceController {

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(StreamingServiceController.class);

    @Value("${broker.streamUrl}")
    private String url; //  = env.getProperty("broker.url");
    @Value("${broker.user}")
    private String user; // =env.getProperty("broker.user");
    @Value("${broker.accessToken}")
    private String accessToken; // =env.getProperty("broker.accessToken");
    @Value("${broker.accountId}")
    private String accountId; // =env.getProperty("broker.accountId");

    private AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url, user, accessToken);

    @RequestMapping(value = "/heartbeat", method = RequestMethod.GET)
    public String streamPriceForInstrument(@RequestParam(value = "instrument") String instrument) throws InterruptedException {

        final String heartbeatSourceId = "PriceInfo";
        TradeableInstrument<String> tradeableInstrument = new TradeableInstrument<String>(instrument);
        Collection<TradeableInstrument<String>> instruments = Lists.newArrayList(tradeableInstrument);

        EventBus eventBus = new EventBus();
        eventBus.register(new DataSubscriber());

        MarketEventCallback<String> mktEventCallback = new MarketEventHandlerImpl<String>(eventBus);
        HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<DateTime>(eventBus);

        MarketDataStreamingService mktDataStreamingService = new BrokerMarketDataStreamingService(url, accessToken,
                accountId, instruments, mktEventCallback, heartBeatCallback, heartbeatSourceId);
        LOG.info("++++++++++++ Starting Market Data Streaming +++++++++++++++++++++");
        mktDataStreamingService.startMarketDataStreaming();
        Thread.sleep(10000L);
        mktDataStreamingService.stopMarketDataStreaming();

        return "";
    }

private static class DataSubscriber {


    @Subscribe
    @AllowConcurrentEvents
    public void handleHeartBeats(HeartBeatPayLoad<DateTime> payLoad) {
        LOG.info(String.format("Heartbeat received @ %s from source %s", payLoad.getHeartBeatPayLoad(), payLoad.getHeartBeatSource()));
    }

    @Subscribe
    @AllowConcurrentEvents
    public void handleMarketDataEvent(MarketDataPayLoad<String> marketDataPayLoad) {
        LOG.info(String.format("TickData event: %s @ %s. Bid Price = %3.5f, Ask Price = %3.5f", marketDataPayLoad
                        .getInstrument().getInstrument(), marketDataPayLoad.getEventDate(),
                marketDataPayLoad.getBidPrice(), marketDataPayLoad.getAskPrice()));
    }

}

}

