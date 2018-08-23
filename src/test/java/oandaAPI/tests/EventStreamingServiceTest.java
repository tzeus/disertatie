/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import oandaAPI.account.OandaAccountDataProviderService;
import oandaAPI.account.OandaJsonKeys;

import oandaAPI.events.OandaEventsStreamingService;

import org.apache.log4j.Logger;

import org.joda.time.DateTime;

import org.json.simple.JSONObject;

import tradingAPI.account.AccountDataProvider;

import tradingAPI.events.EventCallback;
import tradingAPI.events.EventCallbackImpl;
import tradingAPI.events.EventPayLoad;
import tradingAPI.events.EventsStreamingService;

import tradingAPI.heartbeat.HeartBeatCallback;
import tradingAPI.heartbeat.HeartBeatCallbackImpl;


public class EventStreamingServiceTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(EventStreamingServiceTest.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        usage(args);
        String url = args[0];
        String url2 = args[1];
        String userName = args[2];
        String accessToken = args[3];
        final String heartBeatSourceId = "DEMO_EVTDATASTREAM";

        EventBus eventBus = new EventBus();
        eventBus.register(new EventSubscriber());
        HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<DateTime>(eventBus);
        AccountDataProvider<String> accountDataProvider = new OandaAccountDataProviderService(url2, userName, accessToken);
        EventCallback<JSONObject> eventCallback = new EventCallbackImpl<JSONObject>(eventBus);

        EventsStreamingService evtStreamingService = new OandaEventsStreamingService(url, accessToken, accountDataProvider, eventCallback, heartBeatCallback, heartBeatSourceId);
        evtStreamingService.startEventsStreaming();

        Thread.sleep(60000L);
        evtStreamingService.stopEventsStreaming();
    }

    private static void usage(String[] args) {
        if (args.length != 4) {
            LOG.error("Usage: EventsStreamingServiceDemo <url> <url2> <username> <accesstoken>");
            System.exit(1);
        }
    }

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Nested Classes 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static class EventSubscriber {

        @Subscribe
        @AllowConcurrentEvents
        public void handleEvent(EventPayLoad<JSONObject> payLoad) {
            String transactionType = payLoad.getPayLoad().get(OandaJsonKeys.TYPE.value()).toString();
            LOG.info(String.format("Type:%s, payload=%s", transactionType, payLoad.getPayLoad()));
        }
    }
}
