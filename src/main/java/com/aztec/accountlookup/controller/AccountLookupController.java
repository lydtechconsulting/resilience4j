package com.aztec.accountlookup.controller;

import com.aztec.accountlookup.rest.api.AccountLookupResponse;
import com.aztec.accountlookup.router.AccountLookupRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/v1/accountlookup")
@RequiredArgsConstructor
@Slf4j
public class AccountLookupController {

    private final AccountLookupRouter accountLookupRouter;

    @RequestMapping(method = RequestMethod.GET, value = "/account")
    public ResponseEntity<AccountLookupResponse> lookupBank(
            @RequestParam(value = "iban", required = true) final String iban,
            @RequestParam(value = "country", required = true) final String country,
            @RequestParam(value = "currency", required = true) final String currency) {
        log.debug("lookupBank iban: {}, country: {}, currency: {}", iban, country, currency);

        try {
            final AccountLookupResponse account = accountLookupRouter.lookupAccount(iban, country, currency);
            return ResponseEntity.ok(account);
        } catch (final HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }
}
