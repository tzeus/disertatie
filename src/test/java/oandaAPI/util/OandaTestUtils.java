package oandaAPI.util;

import java.io.FileInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.mockito.Mockito;


public class OandaTestUtils {
	private OandaTestUtils() {
	}

	public static final void mockHttpInteraction(String fname, HttpClient mockHttpClient) throws Exception {
		CloseableHttpResponse mockResp = Mockito.mock(CloseableHttpResponse.class);
		Mockito.when(mockHttpClient.execute(Mockito.any(HttpUriRequest.class))).thenReturn(mockResp);

		HttpEntity mockEntity = Mockito.mock(HttpEntity.class);

		Mockito.when(mockResp.getEntity()).thenReturn(mockEntity);

		StatusLine mockStatusLine = Mockito.mock(StatusLine.class);

		Mockito.when(mockResp.getStatusLine()).thenReturn(mockStatusLine);
		Mockito.when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
		Mockito.when(mockEntity.getContent()).thenReturn(new FileInputStream(fname));
	}
}
