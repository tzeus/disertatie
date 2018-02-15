package email;

import tradingAPI.events.EventPayLoad;

public interface EmailContentGenerator<T> {

	EmailPayLoad generate(EventPayLoad<T> payLoad);

}
