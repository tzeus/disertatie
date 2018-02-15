package twitter;

import java.util.Collection;

import tradingAPI.instruments.TradeableInstrument;

public interface TweetHarvester<T> {

	Collection<NewFXTradeTweet<T>> harvestNewTradeTweets(String userId);

	Collection<CloseFXTradeTweet<T>> harvestHistoricTradeTweets(String userId, TradeableInstrument<T> instrument);
}
