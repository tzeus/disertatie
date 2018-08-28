package com.tudoreloprisan.email;

import com.tudoreloprisan.tradingAPI.events.EventPayLoad;

public interface EmailContentGenerator<T> {

	EmailPayLoad generate(EventPayLoad<T> payLoad);

}
