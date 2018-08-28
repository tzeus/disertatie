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

public class MovingAverageTest {

	private static final Logger LOG = Logger.getLogger(MovingAverageTest.class);

	private static void usage(String[] args) {
		if (args.length != 2) {
			LOG.error("Usage: MovingAverageCalculationServiceDemo <url> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		usage(args);
		final String url = args[0];
		final String accessToken = args[1];
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
