package com.aztec.accountlookup.integration;

import com.aztec.accountlookup.rest.api.AccountLookupResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class AccountLookupFailoverIntegrationTest extends BaseAccountLookupIntegrationTest {

    @Test
    public void lookupUpAccount_BankOneSuccess_Test() {
        primeBankOneForSuccess(IBAN, COUNTRY, CURRENCY);

        ResponseEntity<AccountLookupResponse> response = callLookupAccount(IBAN, COUNTRY, CURRENCY);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody().getAccountLookupProvider(), equalTo(BANK_ONE_NAME));
        assertThat(response.getBody().getRoutingNumber(), equalTo(ROUTING_NUMBER));
    }

    /**
     * A 4xx such as a NOT FOUND (404) should not result in a failover to BANK TWO, as the client request needs to be
     * corrected.  So the exception is percolated up and returned to the client.
     */
    @Test
    public void lookupUpAccount_BanksOne_AccountNotFound_Test() {
        primeBankOneForFailure(404);
        // Still set bank two to success, to prove that this is not hit (as would return a 200).
        primeBankTwoForSuccess(IBAN, COUNTRY, CURRENCY);

        ResponseEntity<AccountLookupResponse> response = callLookupAccount(IBAN, COUNTRY, CURRENCY);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    /**
     * A 5xx such as a SERVICE UNAVAILABLE (503) should result in a failover to BANK TWO.
     */
    @Test
    public void lookupUpAccount_BankOneUnavailable_FailoverToBankTwo_Test() {
        primeBankOneForFailure(503);
        primeBankTwoForSuccess(IBAN, COUNTRY, CURRENCY);

        ResponseEntity<AccountLookupResponse> response = callLookupAccount(IBAN, COUNTRY, CURRENCY);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(response.getBody().getAccountLookupProvider(), equalTo(BANK_TWO_NAME));
        assertThat(response.getBody().getRoutingNumber(), equalTo(ROUTING_NUMBER));
    }

    /**
     * If the fallback call fails with a 5xx, then a 500 is returned to the client.
     */
    @Test
    public void lookupUpAccount_BankOneUnavailable_BankTwoBadGateway_Test() {
        primeBankOneForFailure(503);
        primeBankTwoForFailure(502);

        ResponseEntity<AccountLookupResponse> response = callLookupAccount(IBAN, COUNTRY, CURRENCY);

        assertThat(response.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
