package com.tudoreloprisan.brokerAPI.events;

import com.tudoreloprisan.tradingAPI.events.Event;

public enum AccountEvents implements Event {
	MARGIN_CALL_ENTER, MARGIN_CALL_EXIT, TRANSFER_FUNDS, DAILY_FINANCING;
}
