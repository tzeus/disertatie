/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

import email.EventEmailNotifier;

import oandaAPI.account.OandaJsonKeys;

import oandaAPI.events.OrderEvents;

import org.joda.time.DateTime;

import org.json.simple.JSONObject;

import org.junit.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import tradingAPI.events.EventPayLoad;


public class EventEmailNotifierTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void testSendEmail() {
        ApplicationContext appContext = new ClassPathXmlApplicationContext("emailTest.xml");
        EventEmailNotifier<JSONObject> emailNotifier = appContext.getBean(EventEmailNotifier.class);
        EventBus eventBus = new EventBus();
        eventBus.register(emailNotifier);

        Map<String, Object> payload = Maps.newHashMap();
        payload.put(OandaJsonKeys.INSTRUMENT.value(), "GBP_USD");
        payload.put(OandaJsonKeys.TYPE.value(), OrderEvents.ORDER_FILL.toString());
        payload.put(OandaJsonKeys.ACCOUNT_ID.value(), "494-4949-49494-944");
        payload.put(OandaJsonKeys.ACCOUNT_BALANCE.value(), "127.8");
        payload.put(OandaJsonKeys.TIME.value(), DateTime.now().toString());
        payload.put(OandaJsonKeys.PRICE.value(), "1.2222");
        payload.put(OandaJsonKeys.UNITS.value(), "500");

        JSONObject jsonObj = new JSONObject(payload);
        eventBus.post(new EventPayLoad<>(OrderEvents.ORDER_FILL, jsonObj));
    }

}
