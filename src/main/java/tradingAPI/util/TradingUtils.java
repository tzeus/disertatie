package tradingAPI.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;

public class TradingUtils {

	private TradingUtils() {
	}

	public static final int		CCY_PAIR_LEN	= 7;
	public static final int		CCY_SEP_CHR_POS	= 3;
	private static final int	THOUSAND		= 1000;

	/**
	 * 
	 * @param request
	 * @return a String representation of the HttpRequest which will be or is
	 *         already executed. Useful for logging the HTTP request made.
	 */
	public static final String executingRequestMsg(HttpRequest request) {
		return "Executing request : " + request.getRequestLine();
	}

	/**
	 * 
	 * @param dateTime
	 * @return a UNIX representation of the dateTime object which actually is in
	 *         fact a nanosecond representation obtained by multiplying the
	 *         millisecond representation with 1000;
	 */
	public static long toUnixTime(DateTime dateTime) {
		return dateTime.getMillis() * THOUSAND;
	}

	/**
	 * @param nanos
	 * @return millisecond representation of a nanosecond timestamp.
	 */
	public static long toMillisFromNanos(long nanos) {
		return nanos / THOUSAND;
	}

	/**
	 * Utility method to return the currency pair which form the tradeable
	 * instrument. It is effectively a string tokeniser with no validation for
	 * currency or number of tokens or in fact the separator character.
	 * 
	 * input "GBP_USD", "_" will return ["GBP","USD"] input "GBP_USD", "/" will
	 * return ["GBP_USD"] input "hello_GBP_USD", "_" will return
	 * ["hello","GBP","USD"]
	 * 
	 * @param instrument
	 * @param currencySeparator
	 * @return String tokens for instrument which contains currencySeparator.
	 */
	public static String[] splitCcyPair(String instrument, String currencySeparator) {
		return StringUtils.split(instrument, currencySeparator);
	}

	

	/**
	 * A utility method that tries to toString an HttpResponse object. Only when
	 * HTTP status 200 is returned by the server, will this method attempt to
	 * process the response.
	 * 
	 * @param response
	 * @return a String representation of the response if possible else an empty
	 *         string.
	 * @throws IOException
	 */
	public static final String responseToString(HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();
		if ((response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
				|| response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) && entity != null) {
			InputStream stream = entity.getContent();
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			StringBuilder strResp = new StringBuilder();
			while ((line = br.readLine()) != null) {
				strResp.append(line);
			}
			IOUtils.closeQuietly(stream);
			IOUtils.closeQuietly(br);
			return strResp.toString();
		} else {
			return StringUtils.EMPTY;
		}
	}

	/**
	 * A utility method that is an alternative to having to write null and empty
	 * checks for collections at various places in the code. Akin to
	 * StringUtils.isEmpty() which returns true if input String is null or 0
	 * length.
	 * 
	 * @param collection
	 * @return boolean true if collection is null or is empty else false.
	 * @see StringUtils#isEmpty(CharSequence)
	 */
	public static final boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	/**
	 * A utility method that is an alternative to having to write null and empty
	 * checks for maps at various places in the code. Akin to
	 * StringUtils.isEmpty() which returns true if input String is null or 0
	 * length.
	 * 
	 * @param map
	 * @return boolean true if map is null or is empty else false.
	 * @see StringUtils#isEmpty(CharSequence)
	 */
	public static final boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

	/**
	 * A utility method that tries to split an instrument into 2 currencies. The
	 * currencies can be separated by any separator such as '/','-','_' etc but
	 * must be at the 4th position in the String. The length of instrument must
	 * also be exactly 7 chars.
	 * 
	 * @param instrument
	 *            i.e. a currency pair such as GBP_USD, AUD/CHF etc
	 * @return exactly a pair of currencies as base and denominated.
	 * @throws an
	 *             IllegalArgumentException if instrument cannot be split in
	 *             exactly 2 currencies
	 */
	public static String[] splitInstrumentPair(String instrument) {
		if (!StringUtils.isEmpty(instrument) && instrument.length() == CCY_PAIR_LEN) {
			return new String[] { instrument.substring(0, CCY_SEP_CHR_POS), instrument.substring(CCY_SEP_CHR_POS + 1) };
		}
		throw new IllegalArgumentException(String.format("Instrument %s is not of expected length %d", instrument));
	}

	

	public static void closeSilently(CloseableHttpClient httpClient) {
		if (httpClient == null) {
			return;
		}
		try {
			httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints error message received from an HTTP response to System.err. Should
	 * normally be called when the HTTP status <> 200 in order to print out the
	 * cause for non 200 status code.
	 * 
	 * @param response
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void printErrorMsg(HttpResponse response) throws ParseException, IOException {
		String responseString = getResponse(response);
		System.err.println(responseString);
	}

	public static String getResponse(HttpResponse response) throws ParseException, IOException {
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		return responseString;
	}
}
