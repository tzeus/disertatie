package com.tudoreloprisan.tradingAPI.events;

public interface EventHandler<K, T extends EventPayLoad<K>> {

	void handleEvent(T payLoad);
}
