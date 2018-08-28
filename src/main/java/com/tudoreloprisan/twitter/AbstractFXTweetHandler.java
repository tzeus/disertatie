package com.tudoreloprisan.twitter;



import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import com.tudoreloprisan.tradingAPI.account.ProviderHelper;
import com.tudoreloprisan.tradingAPI.instruments.InstrumentService;
import com.tudoreloprisan.tradingAPI.util.TradingUtils;

public abstract class AbstractFXTweetHandler<T> implements FXTweetHandler<T> {

	@Autowired
	protected Twitter				twitter;
	@Autowired
	protected InstrumentService<T>	instrumentService;
	@Autowired
	protected ProviderHelper		providerHelper;

	protected DateTime				startTime								= DateTime.now();
	protected String				startTimeAsStr;
	protected volatile Long			lastTweetId								= null;
	protected final String			userId;
	protected static final String	CLOSED									= "Closed";
	protected static final String	BOUGHT									= "Bought";
	protected static final String	SOLD									= "Sold";
	protected static final String	TP										= "TP";
	protected static final String	SL										= "SL";
	protected static final Logger	LOG										= Logger
			.getLogger(AbstractFXTweetHandler.class);
	static final String				FROM_USER_SINCE_TIME_TWITTER_QRY_TMPL	= "from:%s since:%s";
	static final String				FROM_USER_SINCE_ID_TWITTER_QRY_TMPL		= "from:%s since_id:%d";

	protected AbstractFXTweetHandler(String userId) {
		this.userId = userId;
		setStartTimeAsStr();
	}

	private void setStartTimeAsStr() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
		startTimeAsStr = formatter.print(this.startTime);
	}

	protected abstract NewFXTradeTweet<T> parseNewTrade(String tokens[]);

	protected abstract CloseFXTradeTweet<T> parseCloseTrade(String tokens[]);

	public void setStartTime(DateTime startTime) {
		this.startTime = startTime;
		setStartTimeAsStr();
	}

	@Override
	public String getUserId() {
		return userId;
	}

	private class TweetPredicate implements Predicate<Tweet> {

		@Override
		public boolean apply(Tweet input) {
			return startTime.isBefore(input.getCreatedAt().getTime());
		}

	}

	@Override
	public Collection<Tweet> findNewTweets() {
		SearchResults results = null;
		if (lastTweetId == null) {// find new tweets since the start of the app
			synchronized (this) {
				if (lastTweetId == null) {// double check locking
					results = twitter.searchOperations().search(
							String.format(FROM_USER_SINCE_TIME_TWITTER_QRY_TMPL, getUserId(), this.startTimeAsStr));
					TweetPredicate predicate = new TweetPredicate();
					List<Tweet> filteredTweets = Lists.newArrayList();
					for (Tweet tweet : results.getTweets()) {
						if (predicate.apply(tweet)) {
							if (lastTweetId == null) {
								this.lastTweetId = tweet.getId();
							}
							filteredTweets.add(tweet);
						}
					}
					return filteredTweets;
				}

			}

		}
		results = twitter.searchOperations()
				.search(String.format(FROM_USER_SINCE_ID_TWITTER_QRY_TMPL, getUserId(), lastTweetId));
		List<Tweet> tweets = results.getTweets();
		if (!TradingUtils.isEmpty(tweets)) {
			lastTweetId = tweets.get(0).getId();
		}
		return tweets;

	}
}
