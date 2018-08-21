package brokerAPI.account;

import static org.junit.Assert.*;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.mockito.Mockito;

import brokerAPI.util.BrokerTestConstants;
import brokerAPI.util.BrokerTestUtils;
import tradingAPI.account.Account;

public class BrokerAccountDataProviderServiceTest {

	private BrokerAccountDataProviderService createSpyAndCommonStuff(String fname,
                                                                     BrokerAccountDataProviderService service) throws Exception {
		BrokerAccountDataProviderService spy = Mockito.spy(service);

		CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
		Mockito.when(spy.getHttpClient()).thenReturn(mockHttpClient);

		BrokerTestUtils.mockHttpInteraction(fname, mockHttpClient);

		return spy;
	}

	@Test
	public void allAccountsTest() throws Exception {
		final BrokerAccountDataProviderService service = new BrokerAccountDataProviderService(BrokerTestConstants.URL,
				BrokerTestConstants.USER, BrokerTestConstants.TOKEN);
		assertEquals("https://api-fxtrade.oanda.com/v3/accounts", service.getAllAccountsUrl());
		BrokerAccountDataProviderService spy = createSpyAndCommonStuff("src/test/resources/accountsAll.txt", service);
		spy.getLatestAccountsInfo();
		Mockito.verify(spy, Mockito.times(1)).getSingleAccountUrl(BrokerTestConstants.ACCOUNT_ID_1);
		Mockito.verify(spy, Mockito.times(1)).getSingleAccountUrl(BrokerTestConstants.ACCOUNT_ID_2);
	}

	@Test
	public void accountIdTest() throws Exception {
		final BrokerAccountDataProviderService service = new BrokerAccountDataProviderService(BrokerTestConstants.URL,
				BrokerTestConstants.USER, BrokerTestConstants.TOKEN);
		assertEquals("https://api-fxtrade.oanda.com/v3/accounts/"+BrokerTestConstants.ACCOUNT_ID_1,
				service.getSingleAccountUrl(BrokerTestConstants.ACCOUNT_ID_1));

		BrokerAccountDataProviderService spy = createSpyAndCommonStuff("src/test/resources/accountSingle.txt", service);
		Account<String> accInfo = spy.getLatestAccountInfo(BrokerTestConstants.ACCOUNT_ID_1);
		assertNotNull(accInfo);
		assertEquals("CHF", accInfo.getCurrency());
		assertEquals(0.05, accInfo.getMarginRate(), BrokerTestConstants.PRECISION);
		
	}
}
