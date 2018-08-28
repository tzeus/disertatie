package com.tudoreloprisan.twitter;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;

import com.google.common.collect.Lists;

import com.tudoreloprisan.tradingAPI.instruments.TradeableInstrument;
import com.tudoreloprisan.tradingAPI.trade.TradingSignal;
import com.tudoreloprisan.tradingAPI.util.TradingConstants;

public class SignalFactoryFXTweetHandler extends AbstractFXTweetHandler<String> {
	private static final String	BUY			= "Buy";
	private static final String	SELL		= "Sell";
	private static final String	CLOSE		= "Close";
	private static final String	COLON		= ":";
	private static final String	AT_THE_RATE	= "@";

	/*
	 * New Trade example: Forex Signal | Buy GBPAUD@1.76385 | SL:1.75985 |
	 * TP:1.77185 | 2018.02.14 04:58 GMT | #fx #forex #fb
	 * 
	 * Close Trade example: Forex Signal | Close(TP) Buy NZDCAD@0.92120 |
	 * Profit: +80 pips | 2018.02.14 03:05 GMT | #fx #forex #fb
	 */

	@Override
	protected NewFXTradeTweet<String> parseNewTrade(String tokens[]) {
		String token1[] = tokens[1].trim().split(TradingConstants.SPACE_RGX);
		String action = token1[0];
		String tokens1spl[] = token1[1].split(AT_THE_RATE);
		String tokens2[] = tokens[2].trim().split(COLON);
		String tokens3[] = tokens[3].trim().split(COLON);

		return new NewFXTradeTweet<String>(
				new TradeableInstrument<String>(this.providerHelper.fromIsoFormat(tokens1spl[0])),
				Double.parseDouble(tokens1spl[1]), Double.parseDouble(tokens2[1]), Double.parseDouble(tokens3[1]),
				BUY.equals(action) ? TradingSignal.LONG : TradingSignal.SHORT);
	}

	@Override
	protected CloseFXTradeTweet<String> parseCloseTrade(String tokens[]) {
		String token1[] = tokens[1].trim().split(TradingConstants.SPACE_RGX);
		String tokens1spl[] = token1[token1.length - 1].split(AT_THE_RATE);
		String token2[] = tokens[2].trim().split(TradingConstants.SPACE_RGX);
		return new CloseFXTradeTweet<String>(
				new TradeableInstrument<String>(this.providerHelper.fromIsoFormat(tokens1spl[0])),
				Double.parseDouble(token2[1]), Double.parseDouble(tokens1spl[1]));
	}

	public SignalFactoryFXTweetHandler(String userid) {
		super(userid);
	}

	@Override
	public FXTradeTweet<String> handleTweet(Tweet tweet) {
		String tweetTxt = tweet.getText();
		String tokens[] = StringUtils.split(tweetTxt, TradingConstants.PIPE_CHR);
		if (tokens.length >= 5) {
			String action = tokens[1].trim();
			if (action.startsWith(BUY) || action.startsWith(SELL)) {
				return parseNewTrade(tokens);
			} else if (action.startsWith(CLOSE)) {
				return parseCloseTrade(tokens);
			}
		}
		return null;
	}

	@Override
	public Collection<Tweet> findHistoricPnlTweetsForInstrument(TradeableInstrument<String> instrument) {
		String isoInstr = this.providerHelper.toIsoFormat(instrument.getInstrument());		
		String query = String.format("Profit: OR Loss: from:%s", getUserId(), isoInstr);
		SearchResults results = twitter.searchOperations().search(query);
		List<Tweet> pnlTweets = results.getTweets();
		List<Tweet> filteredPnlTweets = Lists.newArrayList();
		for (Tweet pnlTweet : pnlTweets) {
			if (pnlTweet.getText().contains(isoInstr)) {
				filteredPnlTweets.add(pnlTweet);
			}
		}
		return filteredPnlTweets;
	}
}
