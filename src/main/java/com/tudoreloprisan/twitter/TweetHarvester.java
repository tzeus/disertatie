package com.tudoreloprisan.twitter;

import java.util.Collection;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;

public interface TweetHarvester<T> {

	Collection<NewFXTradeTweet<T>> harvestNewTradeTweets(String userId);

	Collection<CloseFXTradeTweet<T>> harvestHistoricTradeTweets(String userId, TradeableInstrument<T> instrument);
}
