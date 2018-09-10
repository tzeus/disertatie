/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package com.tudoreloprisan.tradingAPI.marketData;

import java.util.List;

import com.tudoreloprisan.repositories.HistoricalData;
import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;

import org.joda.time.DateTime;


public interface HistoricMarketDataProvider<T> {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    List<CandleStick<T>> getCandleSticks(TradeableInstrument<T> instrument, CandleStickGranularity granularity, DateTime from, DateTime to);

    List<CandleStick<T>> getCandleSticks(TradeableInstrument<T> instrument, CandleStickGranularity granularity, int count);

    List<HistoricalData> getHistoricalDataForInstrument(TradeableInstrument<String> instrument, CandleStickGranularity granularity, int count);
}
