package test;

import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import oandaAPI.account.OandaJsonKeys;

public class SimpleTest {
	public static void main(String[] args) {
		String jsonTime = "{\"time\":\"2016-09-20T15:05:50.163791738Z\",\"type\":\"HEARTBEAT\"}";
		JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonTime);
		DateTime heartBeatTime = DateTime.parse( (String) jsonObject.get(OandaJsonKeys.TIME.value()));
		System.out.println(heartBeatTime);
	}
}
