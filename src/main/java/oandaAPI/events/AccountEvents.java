package oandaAPI.events;

import tradingAPI.events.Event;

public enum AccountEvents implements Event {
	MARGIN_CALL_ENTER, MARGIN_CALL_EXIT, TRANSFER_FUNDS, DAILY_FINANCING;
}
