/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.tests;

import oandaAPI.account.OandaJsonKeys;

import org.joda.time.DateTime;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.junit.Test;


public class SimpleTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void simpleTest() {
        String jsonTime = "{\"time\":\"2016-09-20T15:05:50.163791738Z\",\"type\":\"HEARTBEAT\"}";
        JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonTime);
        DateTime heartBeatTime = DateTime.parse((String) jsonObject.get(OandaJsonKeys.TIME.value()));
        System.out.println(heartBeatTime);
    }
}
