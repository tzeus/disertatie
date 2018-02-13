package oandaAPI.events;

import org.json.simple.JSONObject;

import tradingAPI.events.EventPayLoad;

public class AccountEventPayLoad extends EventPayLoad<JSONObject> {

	public AccountEventPayLoad(AccountEvents event, JSONObject payLoad) {
		super(event, payLoad);
	}

}
