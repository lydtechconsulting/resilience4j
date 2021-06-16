package com.aztec.accountlookup.service;

import com.aztec.accountlookup.gateway.banktwo.BankTwoAccountResponse;
import com.aztec.accountlookup.gateway.banktwo.BankTwoGateway;
import com.aztec.accountlookup.rest.api.AccountLookupResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BankTwoAccountLookupService {

    @Autowired
    private BankTwoGateway gateway;

    public AccountLookupResponse lookupAccount(final String iban, final String country, final String currency) {
        final BankTwoAccountResponse response = gateway.accountLookup(iban, country, currency);
        return AccountLookupResponse.builder()
                .accountLookupProvider(response.getAccountLookupProvider())
                .iban(response.getIban())
                .routingNumber(response.getRoutingNumber())
                .build();
    }
}
