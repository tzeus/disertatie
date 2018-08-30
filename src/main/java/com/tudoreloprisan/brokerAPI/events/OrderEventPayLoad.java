package com.tudoreloprisan.brokerAPI.events;

import com.google.gson.JsonObject;
import com.tudoreloprisan.tradingAPI.events.EventPayLoad;

public class OrderEventPayLoad extends EventPayLoad<JsonObject> {

	private final OrderEvents orderEvent;

	public OrderEventPayLoad(OrderEvents event, JsonObject payLoad) {
		super(event, payLoad);
		this.orderEvent = event;
	}

	@Override
	public OrderEvents getEvent() {
		return this.orderEvent;
	}

}
