package com.aztec.accountlookup.integration;

import com.aztec.accountlookup.AccountLookupServiceApplication;
import com.aztec.accountlookup.rest.api.AccountLookupResponse;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.HttpMethod.GET;

@SpringBootTest(classes=AccountLookupServiceApplication.class, webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port=0)
@ActiveProfiles("test")
public abstract class BaseAccountLookupIntegrationTest {

    protected static String CIRCUIT_BREAKER_NAME = "lookupAccount";

    protected static String IBAN = "12345";
    protected static String COUNTRY = "UK";
    protected static String CURRENCY = "GBP";

    protected static final String ROUTING_NUMBER = "1233567890";

    protected static final String BANK_ONE_NAME = "BANK ONE";
    protected static final String BANK_TWO_NAME = "BANK TWO";

    @Autowired
    private CircuitBreakerRegistry registry;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        WireMock.reset();

        transitionToClosedState();
    }

    protected ResponseEntity<AccountLookupResponse> callLookupAccount(String iban, String country, String currency) {
        HttpHeaders headerMap = new HttpHeaders();
        headerMap.add(HttpHeaders.AUTHORIZATION, "Bearer ${UUID.randomUUID().toString()}");
        ResponseEntity response = restTemplate.exchange("/v1/accountlookup/account?iban="+iban+"&country="+country+"&currency="+currency, GET, new HttpEntity<Object>(headerMap), AccountLookupResponse.class);
        return response;
    }

    protected void transitionToClosedState() {
        CircuitBreaker circuitBreaker = registry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        if(circuitBreaker.getState() != CircuitBreaker.State.CLOSED) {
            circuitBreaker.transitionToClosedState();
        }
    }

    protected CircuitBreaker.State getCircuitBreakerStatus() {
        CircuitBreaker circuitBreaker = registry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        return circuitBreaker.getState();
    }

    protected void primeBankOneForFailure(int responseCode) {
        stubFor(get(urlPathEqualTo("/bankone/api/account"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(responseCode)));
    }

    protected void primeBankTwoForFailure(int responseCode) {
        stubFor(get(urlPathEqualTo("/banktwo/api/account"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(responseCode)));
    }

    protected void primeBankOneForSuccess(String iban, String country, String currency) {
        stubFor(get(urlEqualTo("/bankone/api/account?iban="+iban+"&country="+country+"&currency="+currency))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"accountLookupProvider\": \""+ BANK_ONE_NAME +"\", \"iban\": \""+iban+"\", \"routingNumber\": \""+ROUTING_NUMBER+"\"}")));
    }

    protected void primeBankTwoForSuccess(String iban, String country, String currency) {
        stubFor(get(urlEqualTo("/banktwo/api/account?iban="+iban+"&country="+country+"&currency="+currency))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{\"accountLookupProvider\": \""+ BANK_TWO_NAME +"\", \"iban\": \""+iban+"\", \"routingNumber\": \""+ROUTING_NUMBER+"\"}")));
    }
}
