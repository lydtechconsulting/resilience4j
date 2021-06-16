package com.aztec.accountlookup.rest.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountLookupResponse {
    private String iban;

    private String routingNumber;

    private String accountLookupProvider;
}

