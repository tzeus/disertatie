/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.tudoreloprisan.test;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import com.tudoreloprisan.brokerAPI.streaming.BrokerMarketDataStreamingService;
import com.tudoreloprisan.tradingAPI.heartbeat.DefaultHeartBeatService;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatCallback;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatCallbackImpl;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatPayLoad;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.marketData.MarketEventCallback;
import com.tudoreloprisan.tradingAPI.marketData.MarketEventHandlerImpl;
import com.tudoreloprisan.tradingAPI.streaming.HeartBeatStreamingService;

import org.apache.log4j.Logger;

import org.joda.time.DateTime;

import org.junit.Test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;


@PropertySource("classpath:auth.properties")
public class DefaultHeartBeatServiceTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DefaultHeartBeatServiceTest.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Instance fields 
    //~ ----------------------------------------------------------------------------------------------------------------

    private String url = "https://stream-fxpractice.oanda.com/"; //  = env.getProperty("broker.url");
    @Value("${broker.user}")
    private String user = "toprisan"; // =env.getProperty("broker.user");
    @Value("${broker.accessToken}")
    private String accessToken = "5f65b265e3e232fa9cdef534bc112ad3-34841ec230e5b49d758499affb6b41e7"; // =env.getProperty("broker.accessToken");
    @Value("${broker.accountId}")
    private String accountId = "101-004-9126938-001"; // =env.getProperty("broker.accountId");

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    public void main() throws Exception {
        final String heartbeatSourceId = "DEMO_MKTDATASTREAM";

        TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");

        Collection<TradeableInstrument<String>> instruments = Lists.newArrayList(eurusd);

        EventBus eventBus = new EventBus();

        MarketEventCallback<String> mktEventCallback = new MarketEventHandlerImpl<String>(eventBus);
        HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<DateTime>(eventBus);

        BrokerMarketDataStreamingService mktDataStreaminService = new BrokerMarketDataStreamingService(url, accessToken, accountId, instruments, mktEventCallback, heartBeatCallback, heartbeatSourceId);
        mktDataStreaminService.startMarketDataStreaming();
        Collection<HeartBeatStreamingService> heartbeatstreamingLst = Lists.newArrayList();
        heartbeatstreamingLst.add(mktDataStreaminService);
        DefaultHeartBeatService heartBeatService = new DefaultHeartBeatService(heartbeatstreamingLst);
        eventBus.register(heartBeatService);
        eventBus.register(new DataSubscriber());
        heartBeatService.init();

        heartBeatService.warmUpTime = 5000L;
        Thread.sleep(30000L);
        mktDataStreaminService.stopMarketDataStreaming();
        Thread.sleep(20000L);
    }

    private static void usageAndValidation(String[] args) {
        if (args.length != 3) {
            LOG.error("Usage: DefaultHeartBeatServiceTest <url> <accountid> <accesstoken>");
            System.exit(1);
        }
    }

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Nested Classes 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static class DataSubscriber {

        @Subscribe
        @AllowConcurrentEvents
        public void handleHeartBeats(HeartBeatPayLoad<DateTime> payLoad) {
            LOG.info(String.format("Heartbeat received @ %s from source %s", payLoad.getHeartBeatPayLoad(), payLoad.getHeartBeatSource()));
        }

    }

}
