package tradingAPI.streaming;

public interface HeartBeatStreamingService {

	
	void startHeartBeatStreaming();

	
	void stopHeartBeatStreaming();

	
	String getHeartBeatSourceId();
}
