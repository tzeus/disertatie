package com.tudoreloprisan.brokerAPI.events;

import com.google.gson.JsonObject;
import com.tudoreloprisan.tradingAPI.events.EventPayLoad;

public class AccountEventPayLoad extends EventPayLoad<JsonObject> {

	public AccountEventPayLoad(AccountEvents event, JsonObject payLoad) {
		super(event, payLoad);
	}

}
