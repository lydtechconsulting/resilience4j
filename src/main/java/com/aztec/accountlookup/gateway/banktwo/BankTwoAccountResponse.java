package com.aztec.accountlookup.gateway.banktwo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTwoAccountResponse {
    private String iban;

    private String routingNumber;

    private String accountLookupProvider;
}
