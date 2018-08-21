package test;

import java.util.Map;

import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import email.EventEmailNotifier;
import brokerAPI.account.BrokerJsonKeys;
import brokerAPI.events.OrderEvents;
import tradingAPI.events.EventPayLoad;

public class EventEmailNotifierTest {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("emailTest.xml");
		EventEmailNotifier<JSONObject> emailNotifier = appContext.getBean(EventEmailNotifier.class);
		EventBus eventBus = new EventBus();
		eventBus.register(emailNotifier);

		Map<String, Object> payload = Maps.newHashMap();
		payload.put(BrokerJsonKeys.INSTRUMENT.value(), "GBP_USD");
		payload.put(BrokerJsonKeys.TYPE.value(), OrderEvents.ORDER_FILL.toString());
		payload.put(BrokerJsonKeys.ACCOUNT_ID.value(), "494-4949-49494-944");
		payload.put(BrokerJsonKeys.ACCOUNT_BALANCE.value(), "127.8");
		payload.put(BrokerJsonKeys.TIME.value(), DateTime.now().toString());
		payload.put(BrokerJsonKeys.PRICE.value(), "1.2222");
		payload.put(BrokerJsonKeys.UNITS.value(), "500");

		JSONObject jsonObj = new JSONObject(payload);
		eventBus.post(new EventPayLoad<JSONObject>(OrderEvents.ORDER_FILL, jsonObj));
	}

}
