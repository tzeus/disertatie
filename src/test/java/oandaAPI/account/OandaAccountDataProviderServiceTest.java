package oandaAPI.account;

import static org.junit.Assert.*;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.mockito.Mockito;

import oandaAPI.util.OandaTestConstants;
import oandaAPI.util.OandaTestUtils;
import tradingAPI.account.Account;

public class OandaAccountDataProviderServiceTest {

	private OandaAccountDataProviderService createSpyAndCommonStuff(String fname,
			OandaAccountDataProviderService service) throws Exception {
		OandaAccountDataProviderService spy = Mockito.spy(service);

		CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
		Mockito.when(spy.getHttpClient()).thenReturn(mockHttpClient);

		OandaTestUtils.mockHttpInteraction(fname, mockHttpClient);

		return spy;
	}

	@Test
	public void allAccountsTest() throws Exception {
		final OandaAccountDataProviderService service = new OandaAccountDataProviderService(OandaTestConstants.URL,
				OandaTestConstants.USER, OandaTestConstants.TOKEN);
		assertEquals("https://api-fxtrade.oanda.com/v3/accounts", service.getAllAccountsUrl());
		OandaAccountDataProviderService spy = createSpyAndCommonStuff("src/test/resources/accountsAll.txt", service);
		spy.getLatestAccountsInfo();
		Mockito.verify(spy, Mockito.times(1)).getSingleAccountUrl(OandaTestConstants.ACCOUNT_ID_1);
		Mockito.verify(spy, Mockito.times(1)).getSingleAccountUrl(OandaTestConstants.ACCOUNT_ID_2);
	}

	@Test
	public void accountIdTest() throws Exception {
		final OandaAccountDataProviderService service = new OandaAccountDataProviderService(OandaTestConstants.URL,
				OandaTestConstants.USER, OandaTestConstants.TOKEN);
		assertEquals("https://api-fxtrade.oanda.com/v3/accounts/"+OandaTestConstants.ACCOUNT_ID_1, 
				service.getSingleAccountUrl(OandaTestConstants.ACCOUNT_ID_1));

		OandaAccountDataProviderService spy = createSpyAndCommonStuff("src/test/resources/accountSingle.txt", service);
		Account<String> accInfo = spy.getLatestAccountInfo(OandaTestConstants.ACCOUNT_ID_1);
		assertNotNull(accInfo);
		assertEquals("CHF", accInfo.getCurrency());
		assertEquals(0.05, accInfo.getMarginRate(), OandaTestConstants.PRECISION);
		
	}
}
