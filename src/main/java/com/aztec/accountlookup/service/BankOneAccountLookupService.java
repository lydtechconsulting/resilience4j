package com.aztec.accountlookup.service;

import com.aztec.accountlookup.gateway.bankone.rest.BankOneAccountResponse;
import com.aztec.accountlookup.gateway.bankone.rest.BankOneGateway;
import com.aztec.accountlookup.rest.api.AccountLookupResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BankOneAccountLookupService {

    @Autowired
    private BankOneGateway gateway;

    public AccountLookupResponse lookupAccount(final String iban, final String country, final String currency) {
        final BankOneAccountResponse response = gateway.accountLookup(iban, country, currency);
        return AccountLookupResponse.builder()
                .accountLookupProvider(response.getAccountLookupProvider())
                .iban(response.getIban())
                .routingNumber(response.getRoutingNumber())
                .build();
    }
}
