package com.tudoreloprisan.test;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import com.tudoreloprisan.brokerAPI.account.BrokerAccountDataProviderService;
import com.tudoreloprisan.brokerAPI.account.BrokerJsonKeys;
import com.tudoreloprisan.brokerAPI.events.BrokerEventsStreamingService;
import com.tudoreloprisan.tradingAPI.account.AccountDataProvider;
import com.tudoreloprisan.tradingAPI.events.EventCallback;
import com.tudoreloprisan.tradingAPI.events.EventCallbackImpl;
import com.tudoreloprisan.tradingAPI.events.EventPayLoad;
import com.tudoreloprisan.tradingAPI.events.EventsStreamingService;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatCallback;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatCallbackImpl;

public class EventStreamingServiceTest {
	private static final Logger LOG = Logger.getLogger(EventStreamingServiceTest.class);

	private static void usage(String[] args) {
		if (args.length != 4) {
			LOG.error("Usage: EventsStreamingServiceDemo <url> <url2> <username> <accesstoken>");
			System.exit(1);
		}
	}

	private static class EventSubscriber {

		@Subscribe
		@AllowConcurrentEvents
		public void handleEvent(EventPayLoad<JSONObject> payLoad) {
			String transactionType = payLoad.getPayLoad().get(BrokerJsonKeys.TYPE.value()).toString();
			LOG.info(String.format("Type:%s, payload=%s", transactionType, payLoad.getPayLoad()));
		}
	}

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
		AccountDataProvider<String> accountDataProvider = new BrokerAccountDataProviderService(url2, userName,
				accessToken);
		EventCallback<JSONObject> eventCallback = new EventCallbackImpl<JSONObject>(eventBus);

		EventsStreamingService evtStreamingService = new BrokerEventsStreamingService(url, accessToken,
				accountDataProvider, eventCallback, heartBeatCallback, heartBeatSourceId);
		evtStreamingService.startEventsStreaming();
		
		Thread.sleep(60000L);
		evtStreamingService.stopEventsStreaming();
	}
}
