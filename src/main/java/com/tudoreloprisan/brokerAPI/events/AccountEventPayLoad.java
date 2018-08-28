package com.tudoreloprisan.brokerAPI.events;

import org.json.simple.JSONObject;

import com.tudoreloprisan.tradingAPI.events.EventPayLoad;

public class AccountEventPayLoad extends EventPayLoad<JSONObject> {

	public AccountEventPayLoad(AccountEvents event, JSONObject payLoad) {
		super(event, payLoad);
	}

}
