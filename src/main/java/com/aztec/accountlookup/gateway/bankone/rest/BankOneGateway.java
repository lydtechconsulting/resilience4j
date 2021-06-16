package com.aztec.accountlookup.gateway.bankone.rest;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;

import com.aztec.accountlookup.properties.AccountLookupProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Component
public class BankOneGateway {

    private RestTemplate restTemplate;
    private AccountLookupProperties.BankOneConfig config;

    public BankOneGateway(@Autowired AccountLookupProperties properties) {
        this.config = properties.getEndpoints().getBankOne();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MappingJackson2HttpMessageConverter jackson = new MappingJackson2HttpMessageConverter(mapper);
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setReadTimeout(config.getReadTimeoutMillis());
        requestFactory.setConnectTimeout(config.getConnectTimeoutMillis());

        RestTemplate template = new RestTemplate(Collections.singletonList(jackson));
        template.setRequestFactory(requestFactory);

        this.restTemplate = template;
    }

    public BankOneAccountResponse accountLookup(final String iban, final String country, final String currency) {

        try {
            URI uri = UriComponentsBuilder
                    .fromUri(config.getBaseUrl().toURI())
                    .path(config.getAccountLookupPath())
                    .queryParam("iban", iban)
                    .queryParam("country", country)
                    .queryParam("currency", currency)
                    .build(new HashMap<>());

            ResponseEntity<BankOneAccountResponse> response = restTemplate.exchange(uri, GET, new HttpEntity<>(headers()), BankOneAccountResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            // Covers 4xx exceptions.
             log.error("HttpClientErrorException exception thrown in Bank One.", e);
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error searching Bank One for account [%s, %s]", iban, country), e);
        }
    }

    private static HttpHeaders headers() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
