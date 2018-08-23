/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import tradingAPI.instruments.TradeableInstrument;

import twitter.CloseFXTradeTweet;
import twitter.NewFXTradeTweet;
import twitter.TweetHarvester;


public class TweetHarvesterTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Static fields/initializers 
    //~ ----------------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(TweetHarvesterTest.class);

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

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
        LOG.info(String.format("+++++++++ Dumping the first %d of %d new fx tweets for userid %s +++++++", tweetsToDump, newTradeTweets.size(), userId));
        Iterator<NewFXTradeTweet<String>> newTweetItr = newTradeTweets.iterator();
        while (newTweetItr.hasNext() && (ctr < tweetsToDump)) {
            NewFXTradeTweet<String> newFxTweet = newTweetItr.next();
            LOG.info(newFxTweet);
            ctr++;
        }

        Collection<CloseFXTradeTweet<String>> closedTradeTweets = tweetHarvester.harvestHistoricTradeTweets(userId, eurusd);
        ctr = 0;
        Iterator<CloseFXTradeTweet<String>> closedTweetItr = closedTradeTweets.iterator();
        LOG.info(String.format("+++++++++ Dumping the first %d of %d closed fx tweets for userid %s +++++++", tweetsToDump, closedTradeTweets.size(), userId));
        while (closedTweetItr.hasNext() && (ctr < tweetsToDump)) {
            CloseFXTradeTweet<String> closeFxTweet = closedTweetItr.next();
            LOG.info(String.format("Instrument %s, profit = %3.1f, price=%2.5f ", closeFxTweet.getInstrument().getInstrument(), closeFxTweet.getProfit(), closeFxTweet.getPrice()));
            ctr++;
        }

    }

}
