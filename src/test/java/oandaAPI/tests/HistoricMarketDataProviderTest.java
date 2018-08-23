/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import java.util.List;

import oandaAPI.marketData.OandaHistoricMarketDataProvider;

import oandaAPI.util.OandaTestConstants;

import org.apache.log4j.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import org.junit.Test;

import tradingAPI.instruments.TradeableInstrument;

import tradingAPI.marketData.CandleStick;
import tradingAPI.marketData.CandleStickGranularity;
import tradingAPI.marketData.HistoricMarketDataProvider;


public class HistoricMarketDataProviderTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(HistoricMarketDataProviderTest.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void testHistoricDataProvider() {
        HistoricMarketDataProvider<String> historicMarketDataProvider = new OandaHistoricMarketDataProvider(OandaTestConstants.URL, OandaTestConstants.BAGROV_TOKEN);
        TradeableInstrument<String> jpychf = new TradeableInstrument<String>("EUR_CHF");
        List<CandleStick<String>> candlesJpyChf = historicMarketDataProvider.getCandleSticks(jpychf, CandleStickGranularity.M1, 150);
        LOG.info(String.format("++++++++++++++++++ Last %d Candle Sticks with Daily Granularity for %s ++++++++++ ", candlesJpyChf.size(), jpychf.getInstrument()));

        for (CandleStick<String> candle : candlesJpyChf) {
            LOG.info(candle);
        }
//        TradeableInstrument<String> gbpaud = new TradeableInstrument<String>("GBP_AUD");
//        DateTime to = DateTime.now(DateTimeZone.UTC).minusHours(1);
//        DateTime from = to.minusMonths(1);
//        List<CandleStick<String>> candlesGbpAud = historicMarketDataProvider.getCandleSticks(gbpaud, CandleStickGranularity.M, from, to);
//
//        LOG.info(String.format("+++++++++++Candle Sticks From %s To %s with Monthly Granularity for %s ++++++++++ ", from, to, gbpaud.getInstrument()));
//        for (CandleStick<String> candle : candlesGbpAud) {
//            LOG.info(candle);
//        }

    }

    private static void usage(String[] args) {
        if (args.length != 2) {
            LOG.error("Usage: HistoricMarketDataProviderDemo <url> <accesstoken>");
            System.exit(1);
        }
    }

}
