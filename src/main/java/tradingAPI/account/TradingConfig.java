package tradingAPI.account;

public class TradingConfig extends BaseTradingConfig {

	private String	mailTo;
	private int		fadeTheMoveJumpReqdToTrade;
	private int		fadeTheMoveDistanceToTrade;
	private int		fadeTheMovePipsDesired;
	private int		fadeTheMovePriceExpiry;

	public int getFadeTheMovePriceExpiry() {
		return fadeTheMovePriceExpiry;
	}

	public void setFadeTheMovePriceExpiry(int fadeTheMovePriceExpiry) {
		this.fadeTheMovePriceExpiry = fadeTheMovePriceExpiry;
	}

	public String getMailTo() {
		return mailTo;
	}

	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}

	public int getFadeTheMoveJumpReqdToTrade() {
		return fadeTheMoveJumpReqdToTrade;
	}

	public void setFadeTheMoveJumpReqdToTrade(int fadeTheMoveJumpReqdToTrade) {
		this.fadeTheMoveJumpReqdToTrade = fadeTheMoveJumpReqdToTrade;
	}

	public int getFadeTheMoveDistanceToTrade() {
		return fadeTheMoveDistanceToTrade;
	}

	public void setFadeTheMoveDistanceToTrade(int fadeTheMoveDistanceToTrade) {
		this.fadeTheMoveDistanceToTrade = fadeTheMoveDistanceToTrade;
	}

	public int getFadeTheMovePipsDesired() {
		return fadeTheMovePipsDesired;
	}

	public void setFadeTheMovePipsDesired(int fadeTheMovePipsDesired) {
		this.fadeTheMovePipsDesired = fadeTheMovePipsDesired;
	}

}
