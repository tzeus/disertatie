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

import tradingAPI.heartbeat.HeartBeatCallback;
import tradingAPI.heartbeat.HeartBeatCallbackImpl;
import tradingAPI.heartbeat.HeartBeatPayLoad;

import tradingAPI.instruments.TradeableInstrument;

import tradingAPI.marketData.MarketDataPayLoad;
import tradingAPI.marketData.MarketEventCallback;
import tradingAPI.marketData.MarketEventHandlerImpl;

import tradingAPI.streaming.MarketDataStreamingService;


public class MarketDataStreamingServiceTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(MarketDataStreamingServiceTest.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {

        usageAndValidation(args);
        final String url = args[0];
        final String accountId = args[1];
        final String accessToken = args[2];
        final String heartbeatSourceId = "DEMO_MKTDATASTREAM";

        TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");
        TradeableInstrument<String> gbpnzd = new TradeableInstrument<String>("GBP_NZD");

        Collection<TradeableInstrument<String>> instruments = Lists.newArrayList(eurusd, gbpnzd);

        EventBus eventBus = new EventBus();
        eventBus.register(new DataSubscriber());

        MarketEventCallback<String> mktEventCallback = new MarketEventHandlerImpl<String>(eventBus);
        HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<DateTime>(eventBus);

        MarketDataStreamingService mktDataStreaminService = new OandaMarketDataStreamingService(url, accessToken, accountId, instruments, mktEventCallback, heartBeatCallback, heartbeatSourceId);
        LOG.info("++++++++++++ Starting Market Data Streaming +++++++++++++++++++++");
        mktDataStreaminService.startMarketDataStreaming();
        Thread.sleep(10000L);
        mktDataStreaminService.stopMarketDataStreaming();
    }

    private static void usageAndValidation(String[] args) {
        if (args.length != 3) {
            LOG.error("Usage: MarketDataStreamingServiceDemo <url> <accountid> <accesstoken>");
            System.exit(1);
        }
    }

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Nested Classes 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static class DataSubscriber {

        @Subscribe
        @AllowConcurrentEvents
        public void handleMarketDataEvent(MarketDataPayLoad<String> marketDataPayLoad) {
            LOG.info(String.format("TickData event: %s @ %s. Bid Price = %3.5f, Ask Price = %3.5f", marketDataPayLoad.getInstrument().getInstrument(), marketDataPayLoad.getEventDate(), marketDataPayLoad.getBidPrice(),
                    marketDataPayLoad.getAskPrice()));
        }

        @Subscribe
        @AllowConcurrentEvents
        public void handleHeartBeats(HeartBeatPayLoad<DateTime> payLoad) {
            LOG.info(String.format("Heartbeat received @ %s from source %s", payLoad.getHeartBeatPayLoad(), payLoad.getHeartBeatSource()));
        }

    }

}
