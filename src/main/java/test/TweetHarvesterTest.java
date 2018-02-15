package test;

import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import tradingAPI.instruments.TradeableInstrument;
import twitter.CloseFXTradeTweet;
import twitter.NewFXTradeTweet;
import twitter.TweetHarvester;

public class TweetHarvesterTest {
	private static final Logger LOG = Logger.getLogger(TweetHarvesterTest.class);

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		ApplicationContext appContext = new ClassPathXmlApplicationContext("beans-twitter.xml");
		
		TweetHarvester<String> tweetHarvester = appContext.getBean(TweetHarvester.class);

		TradeableInstrument<String> eurusd = new TradeableInstrument<String>("AUD_JPY");
		//String userId = "ZuluTrader101";
		String userId = "SignalFactory";
		final int tweetsToDump = 10;
		int ctr = 0;

		Collection<NewFXTradeTweet<String>> newTradeTweets = tweetHarvester.harvestNewTradeTweets(userId);
		LOG.info(String.format("+++++++++ Dumping the first %d of %d new fx tweets for userid %s +++++++", tweetsToDump,
				newTradeTweets.size(), userId));
		Iterator<NewFXTradeTweet<String>> newTweetItr = newTradeTweets.iterator();
		while (newTweetItr.hasNext() && ctr < tweetsToDump) {
			NewFXTradeTweet<String> newFxTweet = newTweetItr.next();
			LOG.info(newFxTweet);
			ctr++;
		}

		Collection<CloseFXTradeTweet<String>> closedTradeTweets = tweetHarvester.harvestHistoricTradeTweets(userId,
				eurusd);
		ctr = 0;
		Iterator<CloseFXTradeTweet<String>> closedTweetItr = closedTradeTweets.iterator();
		LOG.info(String.format("+++++++++ Dumping the first %d of %d closed fx tweets for userid %s +++++++",
				tweetsToDump, closedTradeTweets.size(), userId));
		while (closedTweetItr.hasNext() && ctr < tweetsToDump) {
			CloseFXTradeTweet<String> closeFxTweet = closedTweetItr.next();
			LOG.info(String.format("Instrument %s, profit = %3.1f, price=%2.5f ",
					closeFxTweet.getInstrument().getInstrument(), closeFxTweet.getProfit(), closeFxTweet.getPrice()));
			ctr++;
		}

	}

}
