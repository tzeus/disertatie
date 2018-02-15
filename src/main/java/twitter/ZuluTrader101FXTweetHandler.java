package twitter;

import java.util.Collection;

import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;

import tradingAPI.instruments.TradeableInstrument;
import tradingAPI.trade.TradingSignal;
import tradingAPI.util.TradingConstants;

public class ZuluTrader101FXTweetHandler extends AbstractFXTweetHandler<String> {

	/*
	 * New Trade examples: 
	 * Bought 4.13 Lots #GBPCAD 1.61669 SL 1.60406 TP
	 * 1.65032 | Auto-copy FREE at http://goo.gl/moaYzx #Forex #Finance #Money
	 * 
	 * Close Trade examples: Closed Buy 4.13 Lots #GBPCAD 1.61669 for +40.4
	 * pips, total for today +61.3 pips
	 */

	protected ZuluTrader101FXTweetHandler(String userId) {
		super(userId);
	}

	protected int idxOfTP(String[] tokens) {
		int idx = 0;
		for (String token : tokens) {
			if ("TP".equals(token)) {
				return idx;
			}
			idx++;
		}
		return -1;
	}

	protected int idxOfSL(String[] tokens) {
		int idx = 0;
		for (String token : tokens) {
			if ("SL".equals(token)) {
				return idx;
			}
			idx++;
		}
		return -1;
	}

	@Override
	protected NewFXTradeTweet<String> parseNewTrade(String[] tokens) {

		String currencyPair = null;
		try {
			String ccyWithHashTag = tokens[3];
			currencyPair = this.providerHelper.fromHashTagCurrency(ccyWithHashTag);
			double price = Double.parseDouble(tokens[4]);
			TradingSignal signal = BOUGHT.equals(tokens[0]) ? TradingSignal.LONG : TradingSignal.SHORT;
			double stopLoss = 0.0;
			double takeProfit = 0.0;
			int idxTp = idxOfTP(tokens);
			if (idxTp != -1) {
				takeProfit = Double.parseDouble(tokens[idxTp + 1]);
			}
			int idxSl = idxOfSL(tokens);
			if (idxSl != -1) {
				stopLoss = Double.parseDouble(tokens[idxSl + 1]);
			}
			return new NewFXTradeTweet<String>(new TradeableInstrument<String>(currencyPair), price, stopLoss,
					takeProfit, signal);
		} catch (Exception e) {
			LOG.info(String.format(" got err %s parsing tweet tokens for new trade ", e.getMessage()));
			return null;
		}

	}

	@Override
	protected CloseFXTradeTweet<String> parseCloseTrade(String[] tokens) {

		String currencyPair = null;
		try {
			String ccyWithHashTag = tokens[4];
			currencyPair = this.providerHelper.fromHashTagCurrency(ccyWithHashTag);
			String strPnlPips = tokens[7];
			return new CloseFXTradeTweet<String>(new TradeableInstrument<String>(currencyPair),
					Double.parseDouble(strPnlPips), Double.parseDouble(tokens[5]));
		} catch (Exception e) {
			LOG.info(String.format(" got err %s parsing tweet tokens for close trade:", e.getMessage()));
			return null;
		}

	}

	@Override
	public FXTradeTweet<String> handleTweet(Tweet tweet) {
		String tweetTxt = tweet.getText();
		String tokens[] = tweetTxt.trim().split(TradingConstants.SPACE_RGX);
		if (tweetTxt.startsWith(CLOSED)) {
			return parseCloseTrade(tokens);
		} else if (tweetTxt.startsWith(BOUGHT) || tweetTxt.startsWith(SOLD)) {
			return parseNewTrade(tokens);
		}
		return null;
	}

	@Override
	public Collection<Tweet> findHistoricPnlTweetsForInstrument(TradeableInstrument<String> instrument) {
		String isoInstr = TradingConstants.HASHTAG + this.providerHelper.toIsoFormat(instrument.getInstrument());
		SearchResults results = twitter.searchOperations()
				.search(String.format("from:%s \"Closed Buy\" OR \"Closed Sell\" %s", getUserId(), isoInstr));
		return results.getTweets();
	}

}
