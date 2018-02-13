package oandaAPI.events;

import org.json.simple.JSONObject;

import tradingAPI.events.EventPayLoad;

public class OrderEventPayLoad extends EventPayLoad<JSONObject> {

	private final OrderEvents orderEvent;

	public OrderEventPayLoad(OrderEvents event, JSONObject payLoad) {
		super(event, payLoad);
		this.orderEvent = event;
	}

	@Override
	public OrderEvents getEvent() {
		return this.orderEvent;
	}

}
