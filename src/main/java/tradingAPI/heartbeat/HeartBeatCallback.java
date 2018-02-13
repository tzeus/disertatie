package tradingAPI.heartbeat;

public interface HeartBeatCallback<T> {

	void onHeartBeat(HeartBeatPayLoad<T> payLoad);

}
