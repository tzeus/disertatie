/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import oandaAPI.marketData.OandaHistoricMarketDataProvider;

import oandaAPI.util.OandaTestConstants;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import org.junit.Test;

import tradingAPI.instruments.TradeableInstrument;

import tradingAPI.marketData.CandleStickGranularity;
import tradingAPI.marketData.HistoricMarketDataProvider;
import tradingAPI.marketData.MovingAverageCalculationService;


public class MovingAverageTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(MovingAverageTest.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void testMA() {
        final String url = OandaTestConstants.URL;
        final String accessToken = OandaTestConstants.BAGROV_TOKEN;
        HistoricMarketDataProvider<String> historicMarketDataProvider = new OandaHistoricMarketDataProvider(url, accessToken);
        MovingAverageCalculationService<String> movingAverageCalcService = new MovingAverageCalculationService<String>(historicMarketDataProvider);
        TradeableInstrument<String> eurnzd = new TradeableInstrument<String>("EUR_NZD");
        final int countIntervals = 30;
        ImmutablePair<Double, Double> eurnzdSmaAndWma = movingAverageCalcService.calculateSMAandWMAasPair(eurnzd, countIntervals, CandleStickGranularity.H1);

        LOG.info(String.format("SMA=%2.5f,WMA=%2.5f for instrument=%s,granularity=%s for the last %d intervals", eurnzdSmaAndWma.left, eurnzdSmaAndWma.right, eurnzd.getInstrument(), CandleStickGranularity.H1,
                countIntervals));
        DateTime to = DateTime.now(DateTimeZone.UTC).minusHours(1);
        DateTime from = to.minusMonths(2);

        TradeableInstrument<String> gbpchf = new TradeableInstrument<String>("GBP_CHF");
        ImmutablePair<Double, Double> gbpchfSmaAndWma = movingAverageCalcService.calculateSMAandWMAasPair(gbpchf, from, to, CandleStickGranularity.W);

        LOG.info(String.format("SMA=%2.5f,WMA=%2.5f for instrument=%s,granularity=%s from %s to %s", gbpchfSmaAndWma.left, gbpchfSmaAndWma.right, gbpchf.getInstrument(), CandleStickGranularity.W, from, to));

    }

    private static void usage(String[] args) {
        if (args.length != 2) {
            LOG.error("Usage: MovingAverageCalculationServiceDemo <url> <accesstoken>");
            System.exit(1);
        }
    }
}
