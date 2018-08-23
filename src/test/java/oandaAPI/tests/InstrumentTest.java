/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import java.util.Collection;

import oandaAPI.instruments.OandaInstrumentDataProviderService;

import oandaAPI.util.OandaTestConstants;

import org.apache.log4j.Logger;

import org.junit.Test;

import tradingAPI.instruments.InstrumentDataProvider;
import tradingAPI.instruments.InstrumentService;
import tradingAPI.instruments.TradeableInstrument;


public class InstrumentTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(InstrumentTest.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void testInstrumentService() {
        String url = OandaTestConstants.URL;
        String accountId = OandaTestConstants.ACCOUNT_ID;
        String accessToken = OandaTestConstants.BAGROV_TOKEN;

        InstrumentDataProvider<String> instrumentDataProvider = new OandaInstrumentDataProviderService(url, accountId, accessToken);

        InstrumentService<String> instrumentService = new InstrumentService<String>(instrumentDataProvider);

        Collection<TradeableInstrument<String>> gbpInstruments = instrumentService.getAllPairsWithCurrency("GBP");

        LOG.info("+++++++++++++++++++++++++++++++ Dumping Instrument Info +++++++++++++++++++++++++++++");
        for (TradeableInstrument<String> instrument : gbpInstruments) {
            LOG.info(instrument);
        }
        LOG.info("+++++++++++++++++++++++Finished Dumping Instrument Info +++++++++++++++++++++++++++++");
        TradeableInstrument<String> euraud = new TradeableInstrument<String>("EUR_AUD");
        TradeableInstrument<String> usdjpy = new TradeableInstrument<String>("USD_JPY");
        TradeableInstrument<String> usdzar = new TradeableInstrument<String>("USD_ZAR");

        Double usdjpyPip = instrumentService.getPipForInstrument(usdjpy);
        Double euraudPip = instrumentService.getPipForInstrument(euraud);
        Double usdzarPip = instrumentService.getPipForInstrument(usdzar);

        LOG.info(String.format("Pip for instrument %s is %1.5f", euraud.getInstrument(), euraudPip));
        LOG.info(String.format("Pip for instrument %s is %1.5f", usdjpy.getInstrument(), usdjpyPip));
        LOG.info(String.format("Pip for instrument %s is %1.5f", usdzar.getInstrument(), usdzarPip));
    }

    private static void usageAndValidation(String[] args) {
        if (args.length != 3) {
            LOG.error("Usage: InstrumentServiceDemo <url> <accountid> <accesstoken>");
            System.exit(1);
        }
    }

}
