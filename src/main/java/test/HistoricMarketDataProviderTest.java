package test;

import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import oandaAPI.marketData.OandaHistoricMarketDataProvider;
import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.marketData.CandleStick;
import tradingAPI.marketData.CandleStickGranularity;
import tradingAPI.marketData.HistoricMarketDataProvider;

public class HistoricMarketDataProviderTest {

	private static final Logger LOG = Logger.getLogger(HistoricMarketDataProviderTest.class);

	private static void usage(String[] args) {
		if (args.length != 2) {
			LOG.error("Usage: HistoricMarketDataProviderDemo <url> <accesstoken>");
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		usage(args);
		final String url = args[0];
		final String accessToken = args[1];
		HistoricMarketDataProvider<String> historicMarketDataProvider = new OandaHistoricMarketDataProvider(url,
				accessToken);
		TradeableInstrument<String> usdchf = new TradeableInstrument<String>("USD_CHF");
		List<CandleStick<String>> candlesUsdChf = historicMarketDataProvider.getCandleSticks(usdchf,
				CandleStickGranularity.D, 15);
		LOG.info(String.format("++++++++++++++++++ Last %d Candle Sticks with Daily Granularity for %s ++++++++++ ",
				candlesUsdChf.size(), usdchf.getInstrument()));

		for (CandleStick<String> candle : candlesUsdChf) {
			LOG.info(candle);
		}
		TradeableInstrument<String> gbpaud = new TradeableInstrument<String>("GBP_AUD");
		DateTime to = DateTime.now(DateTimeZone.UTC).minusHours(1);
		DateTime from = to.minusMonths(1);		
		List<CandleStick<String>> candlesGbpAud = historicMarketDataProvider.getCandleSticks(gbpaud,
				CandleStickGranularity.M, from, to);

		LOG.info(String.format("+++++++++++Candle Sticks From %s To %s with Monthly Granularity for %s ++++++++++ ",
				from, to, gbpaud.getInstrument()));
		for (CandleStick<String> candle : candlesGbpAud) {
			LOG.info(candle);
		}

	}

}
