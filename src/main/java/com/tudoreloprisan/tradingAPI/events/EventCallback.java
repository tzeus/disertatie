package com.tudoreloprisan.tradingAPI.events;

public interface EventCallback<T> {

	void onEvent(EventPayLoad<T> eventPayLoad);
}
