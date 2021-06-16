package com.aztec.accountlookup.integration;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.aztec.accountlookup.rest.api.AccountLookupResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
public class AccountLookupCircuitBreakerIntegrationTest extends BaseAccountLookupIntegrationTest {

    @Test
    public void lookupUpAccount_CircuitBreakerStateTransitions_Test() throws Exception {
        primeBankOneForFailure(503);
        primeBankTwoForSuccess(IBAN, COUNTRY, CURRENCY);

        // First 10 calls failover, but circuit breaker remains CLOSED.
        IntStream.range(1, 10).forEach($ -> {
            ResponseEntity<AccountLookupResponse> response = callLookupAccount(IBAN, COUNTRY, CURRENCY);
            log.debug("Iteration: {} - Circuit breaker state: {}", $, getCircuitBreakerStatus());
            performAssertions(response, HttpStatus.OK, BANK_TWO_NAME, CircuitBreaker.State.CLOSED);
        });

        // The circuit breaker limit is reached, causing it to OPEN on the next request that fails.  See: ringBufferSizeInClosedState
        // 10 more calls happen with the circuit breaker remaining OPEN.
        IntStream.range(11, 20).forEach($ -> {
            ResponseEntity<AccountLookupResponse> response = callLookupAccount(IBAN, COUNTRY, CURRENCY);
            log.debug("Iteration: {} - Circuit breaker state: {}", $, getCircuitBreakerStatus());
            performAssertions(response, HttpStatus.OK, BANK_TWO_NAME, CircuitBreaker.State.OPEN);
        });

        // Now wait for 3 seconds before the next request.  At this point the circuit breaker moves to HALF_OPEN.  See: waitDurationInOpenState
        TimeUnit.SECONDS.sleep(3);
        // 5 calls happen with the circuit breaker at HALF_OPEN.
        IntStream.range(21, 25).forEach($ -> {
            ResponseEntity<AccountLookupResponse> response = callLookupAccount(IBAN, COUNTRY, CURRENCY);
            log.debug("Iteration: {} - Circuit breaker state: {}", $, getCircuitBreakerStatus());
            performAssertions(response, HttpStatus.OK, BANK_TWO_NAME, CircuitBreaker.State.HALF_OPEN);
        });

        // Those 5 calls failed over so the circuit breaker OPENs again on the next request that fails.  See: ringBufferSizeInHalfOpenState
        IntStream.range(26, 27).forEach($ -> {
            ResponseEntity<AccountLookupResponse> response = callLookupAccount(IBAN, COUNTRY, CURRENCY);
            log.debug("Iteration: {} - Circuit breaker state: {}", $, getCircuitBreakerStatus());
            performAssertions(response, HttpStatus.OK, BANK_TWO_NAME, CircuitBreaker.State.OPEN);
        });

        // Now BANK_ONE is able to successfully respond to the request.  But while the circuit is OPEN, it will not be hit.
        primeBankOneForSuccess(IBAN, COUNTRY, CURRENCY);
        IntStream.range(28, 30).forEach($ -> {
            ResponseEntity<AccountLookupResponse> response = callLookupAccount(IBAN, COUNTRY, CURRENCY);
            log.debug("Iteration: {} - Circuit breaker state: {}", $, getCircuitBreakerStatus());
            performAssertions(response, HttpStatus.OK, BANK_TWO_NAME, CircuitBreaker.State.OPEN);
        });

        // Now wait for 3 seconds before the next request.  At this point the circuit breaker moves to HALF_OPEN.  See: waitDurationInOpenState
        TimeUnit.SECONDS.sleep(3);
        IntStream.range(31, 35).forEach($ -> {
            ResponseEntity<AccountLookupResponse> response = callLookupAccount(IBAN, COUNTRY, CURRENCY);
            log.debug("Iteration: {} - Circuit breaker state: {}", $, getCircuitBreakerStatus());
            performAssertions(response, HttpStatus.OK, BANK_ONE_NAME, CircuitBreaker.State.HALF_OPEN);
        });

        // Those 5 calls were handled by BANK_ONE so the circuit breaker CLOSEs on the next request that succeeds.  See: ringBufferSizeInHalfOpenState
        IntStream.range(36, 40).forEach($ -> {
            ResponseEntity<AccountLookupResponse> response = callLookupAccount(IBAN, COUNTRY, CURRENCY);
            log.debug("Iteration: {} - Circuit breaker state: {}", $, getCircuitBreakerStatus());
            performAssertions(response, HttpStatus.OK, BANK_ONE_NAME, CircuitBreaker.State.CLOSED);
        });
    }

    private void performAssertions(ResponseEntity<AccountLookupResponse> response,
                                  HttpStatus httpStatus,
                                  String lookupProvider,
                                  CircuitBreaker.State circuitBreaker) {
        assertThat(response.getStatusCode(), equalTo(httpStatus));
        assertThat(response.getBody().getAccountLookupProvider(), equalTo(lookupProvider));
        assertThat(getCircuitBreakerStatus(), equalTo(circuitBreaker));
    }
}
