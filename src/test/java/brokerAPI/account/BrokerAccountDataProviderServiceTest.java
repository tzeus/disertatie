/**
 *  Copyright Murex S.A.S., 2003-2018. All Rights Reserved.
 * 
 *  This software program is proprietary and confidential to Murex S.A.S and its affiliates ("Murex") and, without limiting the generality of the foregoing reservation of rights, shall not be accessed, used, reproduced or distributed without the
 *  express prior written consent of Murex and subject to the applicable Murex licensing terms. Any modification or removal of this copyright notice is expressly prohibited.
 */
package brokerAPI.account;

import brokerAPI.util.BrokerTestConstants;
import brokerAPI.util.BrokerTestUtils;

import org.apache.http.impl.client.CloseableHttpClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;

import org.mockito.Mockito;

import tradingAPI.account.Account;


public class BrokerAccountDataProviderServiceTest {

    //~ ----------------------------------------------------------------------------------------------------------------
    //~ Methods 
    //~ ----------------------------------------------------------------------------------------------------------------

    @Test
    public void allAccountsTest() throws Exception {
        final BrokerAccountDataProviderService service = new BrokerAccountDataProviderService(BrokerTestConstants.URL, BrokerTestConstants.USER, BrokerTestConstants.TOKEN); //TODO Get correct Token Here
        assertEquals("https://api-fxtrade.oanda.com/v3/accounts", service.getAllAccountsUrl());
        BrokerAccountDataProviderService spy = createSpyAndCommonStuff("src/test/resources/accountsAll.txt", service);
        spy.getLatestAccountsInfo();
        Mockito.verify(spy, Mockito.times(1)).getSingleAccountUrl(BrokerTestConstants.ACCOUNT_ID_1);
        Mockito.verify(spy, Mockito.times(1)).getSingleAccountUrl(BrokerTestConstants.ACCOUNT_ID_2);
    }


    private BrokerAccountDataProviderService createSpyAndCommonStuff(String fname, BrokerAccountDataProviderService service) throws Exception {
        BrokerAccountDataProviderService spy = Mockito.spy(service);

        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        Mockito.when(spy.getHttpClient()).thenReturn(mockHttpClient);

        BrokerTestUtils.mockHttpInteraction(fname, mockHttpClient);

        return spy;
    }
}
