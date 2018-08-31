package com.tudoreloprisan.test;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.tudoreloprisan.brokerAPI.marketData.BrokerHistoricMarketDataProvider;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.marketData.CandleStickGranularity;
import com.tudoreloprisan.tradingAPI.marketData.HistoricMarketDataProvider;
import com.tudoreloprisan.tradingAPI.marketData.MovingAverageCalculationService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

public class MovingAverageTest {

	private String url = "https://api-fxpractice.oanda.com/"; //  = env.getProperty("broker.url");
	@Value("${broker.user}")
	private String user = "toprisan"; // =env.getProperty("broker.user");
	@Value("${broker.accessToken}")
	private String accessToken = "5f65b265e3e232fa9cdef534bc112ad3-34841ec230e5b49d758499affb6b41e7"; // =env.getProperty("broker.accessToken");
	@Value("${broker.accountId}")
	private String accountId = "101-004-9126938-001"; // =env.getProperty("broker.accountId");

	private static final Logger LOG = Logger.getLogger(MovingAverageTest.class);

	private static void usage(String[] args) {
		if (args.length != 2) {
			LOG.error("Usage: MovingAverageCalculationServiceDemo <url> <accesstoken>");
			System.exit(1);
		}
	}

	@Test
	public void testMovingAverage() {
		HistoricMarketDataProvider<String> historicMarketDataProvider = new BrokerHistoricMarketDataProvider(url,
				accessToken);
		MovingAverageCalculationService<String> movingAverageCalcService = new MovingAverageCalculationService<String>(
				historicMarketDataProvider);
		TradeableInstrument<String> eurnzd = new TradeableInstrument<String>("EUR_NZD");
		final int countIntervals = 30;
		ImmutablePair<Double, Double> eurnzdSmaAndWma = movingAverageCalcService.calculateSMAandWMAasPair(eurnzd,
				countIntervals, CandleStickGranularity.H1);

		LOG.info(String.format("SMA=%2.5f,WMA=%2.5f for instrument=%s,granularity=%s for the last %d intervals",
				eurnzdSmaAndWma.left, eurnzdSmaAndWma.right, eurnzd.getInstrument(), CandleStickGranularity.H1,
				countIntervals));
		DateTime to = DateTime.now(DateTimeZone.UTC).minusHours(1);
		DateTime from = to.minusMonths(2);

		TradeableInstrument<String> gbpchf = new TradeableInstrument<String>("GBP_CHF");
		ImmutablePair<Double, Double> gbpchfSmaAndWma = movingAverageCalcService.calculateSMAandWMAasPair(gbpchf, from,
				to, CandleStickGranularity.W);

		LOG.info(String
				.format("SMA=%2.5f,WMA=%2.5f for instrument=%s,granularity=%s from %s to %s", gbpchfSmaAndWma.left,
						gbpchfSmaAndWma.right, gbpchf.getInstrument(), CandleStickGranularity.W, from, to));

	}
}
