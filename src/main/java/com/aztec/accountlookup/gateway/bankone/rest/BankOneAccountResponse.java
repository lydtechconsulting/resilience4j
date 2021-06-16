package com.aztec.accountlookup.gateway.bankone.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankOneAccountResponse {
    private String iban;

    private String routingNumber;

    private String accountLookupProvider;
}
