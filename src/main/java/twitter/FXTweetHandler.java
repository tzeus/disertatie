package twitter;

import java.util.Collection;

import org.springframework.social.twitter.api.Tweet;

import tradingAPI.instruments.TradeableInstrument;

public interface FXTweetHandler<T> {

	FXTradeTweet<T> handleTweet(Tweet tweet);

	String getUserId();

	Collection<Tweet> findNewTweets();

	Collection<Tweet> findHistoricPnlTweetsForInstrument(TradeableInstrument<T> instrument);
}
