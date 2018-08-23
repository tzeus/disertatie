/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package oandaAPI.account;

import oandaAPI.util.OandaTestConstants;
import oandaAPI.util.OandaTestUtils;

import org.apache.http.impl.client.CloseableHttpClient;

import static org.junit.Assert.*;
import org.junit.Test;

import org.mockito.Mockito;

import tradingAPI.account.Account;


public class OandaAccountDataProviderServiceTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void allAccountsTest() throws Exception {
        final OandaAccountDataProviderService service = new OandaAccountDataProviderService(OandaTestConstants.URL, OandaTestConstants.USER, OandaTestConstants.TOKEN);
        assertEquals("https://api-fxtrade.oanda.com/v3/accounts", service.getAllAccountsUrl());
        OandaAccountDataProviderService spy = createSpyAndCommonStuff("src/test/resources/accountsAll.txt", service);
        spy.getLatestAccountsInfo();
        Mockito.verify(spy, Mockito.times(1)).getSingleAccountUrl(OandaTestConstants.ACCOUNT_ID_1);
        Mockito.verify(spy, Mockito.times(1)).getSingleAccountUrl(OandaTestConstants.ACCOUNT_ID_2);
    }

    @Test
    public void accountIdTest() throws Exception {
        final OandaAccountDataProviderService service = new OandaAccountDataProviderService(OandaTestConstants.URL, OandaTestConstants.USER, OandaTestConstants.TOKEN);
        assertEquals("https://api-fxtrade.oanda.com/v3/accounts/" + OandaTestConstants.ACCOUNT_ID_1, service.getSingleAccountUrl(OandaTestConstants.ACCOUNT_ID_1));

        OandaAccountDataProviderService spy = createSpyAndCommonStuff("src/test/resources/accountSingle.txt", service);
        Account<String> accInfo = spy.getLatestAccountInfo(OandaTestConstants.ACCOUNT_ID_1);
        assertNotNull(accInfo);
        assertEquals("CHF", accInfo.getCurrency());
        assertEquals(0.02, accInfo.getMarginRate(), OandaTestConstants.PRECISION);

    }

    private OandaAccountDataProviderService createSpyAndCommonStuff(String fname, OandaAccountDataProviderService service) throws Exception {
        OandaAccountDataProviderService spy = Mockito.spy(service);

        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(spy.getHttpClient()).thenReturn(mockHttpClient);

        OandaTestUtils.mockHttpInteraction(fname, mockHttpClient);

        return spy;
    }
}
