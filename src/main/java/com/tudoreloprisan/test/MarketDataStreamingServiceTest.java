package com.tudoreloprisan.test;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import com.tudoreloprisan.brokerAPI.streaming.BrokerMarketDataStreamingService;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatCallback;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatCallbackImpl;
import com.tudoreloprisan.tradingAPI.heartbeat.HeartBeatPayLoad;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.marketData.MarketDataPayLoad;
import com.tudoreloprisan.tradingAPI.marketData.MarketEventCallback;
import com.tudoreloprisan.tradingAPI.marketData.MarketEventHandlerImpl;
import com.tudoreloprisan.tradingAPI.streaming.MarketDataStreamingService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

public class MarketDataStreamingServiceTest {
	private static final Logger LOG = Logger.getLogger(MarketDataStreamingServiceTest.class);

	private static void usageAndValidation(String[] args) {
		if (args.length != 3) {
			LOG.error("Usage: MarketDataStreamingServiceDemo <url> <accountid> <accesstoken>");
			System.exit(1);
		} 
	}

	private static class DataSubscriber {

		@Subscribe
		@AllowConcurrentEvents
		public void handleMarketDataEvent(MarketDataPayLoad<String> marketDataPayLoad) {
			LOG.info(String.format("TickData event: %s @ %s. Bid Price = %3.5f, Ask Price = %3.5f", marketDataPayLoad
					.getInstrument().getInstrument(), marketDataPayLoad.getEventDate(),
					marketDataPayLoad.getBidPrice(), marketDataPayLoad.getAskPrice()));
		}

		@Subscribe
		@AllowConcurrentEvents
		public void handleHeartBeats(HeartBeatPayLoad<DateTime> payLoad) {
			LOG.info(String.format("Heartbeat received @ %s from source %s", payLoad.getHeartBeatPayLoad(), payLoad
					.getHeartBeatSource()));
		}

	}

	@Test
	public void testStreamingData() throws Exception {

		String url = "https://stream-fxpractice.oanda.com/"; //  = env.getProperty("broker.url");
		String user = "toprisan"; // =env.getProperty("broker.user");
		String accessToken = "5f65b265e3e232fa9cdef534bc112ad3-34841ec230e5b49d758499affb6b41e7"; // =env.getProperty("broker.accessToken");
		String accountId = "101-004-9126938-001"; // =env.getProperty("broker.accountId");
		final String heartbeatSourceId = "HEARTBEAT_SERVICE_TEST";

		TradeableInstrument<String> eurusd = new TradeableInstrument<String>("EUR_USD");
		TradeableInstrument<String> gbpnzd = new TradeableInstrument<String>("GBP_NZD");

		Collection<TradeableInstrument<String>> instruments = Lists.newArrayList(eurusd, gbpnzd);

		EventBus eventBus = new EventBus();
		eventBus.register(new DataSubscriber());

		MarketEventCallback<String> mktEventCallback = new MarketEventHandlerImpl<String>(eventBus);
		HeartBeatCallback<DateTime> heartBeatCallback = new HeartBeatCallbackImpl<DateTime>(eventBus);

		MarketDataStreamingService mktDataStreaminService = new BrokerMarketDataStreamingService(url, accessToken,
				accountId, instruments, mktEventCallback, heartBeatCallback, heartbeatSourceId);
		LOG.info("++++++++++++ Starting Market Data Streaming +++++++++++++++++++++");
		mktDataStreaminService.startMarketDataStreaming();
		Thread.sleep(10000L);
		mktDataStreaminService.stopMarketDataStreaming();
	}

}
