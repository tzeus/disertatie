/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import oandaAPI.streaming.OandaMarketDataStreamingService;

import org.apache.log4j.Logger;

import org.joda.time.DateTime;

import org.junit.Test;

import tradingAPI.heartbeat.DefaultHeartBeatService;
import tradingAPI.heartbeat.HeartBeatCallback;
import tradingAPI.heartbeat.HeartBeatCallbackImpl;
import tradingAPI.heartbeat.HeartBeatPayLoad;

import tradingAPI.instruments.TradeableInstrument;

import tradingAPI.marketData.MarketEventCallback;
import tradingAPI.marketData.MarketEventHandlerImpl;

import tradingAPI.streaming.HeartBeatStreamingService;


public class DefaultHeartBeatServiceTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(DefaultHeartBeatServiceTest.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void TestHeartBeatService() throws InterruptedException {
        String url = "https://api-fxpractice.oanda.com";
        String username = "bagrov";
        String accountId = "101-004-7750633-001";
        String accessToken = "ad636147722167721aab6cf1550624ff-9d14ce6efe8fa5f1bff7d976ae492673";
//        final String url = args[0];
//        final String accountId = args[1];
//        final String accessToken = args[2];
        final String heartbeatSourceId = "DEMO_MKTDATASTREAM";

        TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");

        Collection<TradeableInstrument<String>> instruments = Lists.newArrayList(eurusd);

        EventBus eventBus = new EventBus();

        MarketEventCallback<String> mktEventCallback = new MarketEventHandlerImpl<String>(eventBus);
        HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<DateTime>(eventBus);

        OandaMarketDataStreamingService mktDataStreaminService = new OandaMarketDataStreamingService(url, accessToken, accountId, instruments, mktEventCallback, heartBeatCallback, heartbeatSourceId);
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
