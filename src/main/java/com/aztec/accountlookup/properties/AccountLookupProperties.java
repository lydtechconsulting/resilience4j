package com.aztec.accountlookup.properties;

import java.net.URL;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties("account-lookup")
@Getter
@Setter
@Validated
public class AccountLookupProperties {
    @NotNull
    @Valid
    private Endpoints endpoints;

    @Getter
    @Setter
    public static class Endpoints {
        @NotNull
        @Valid
        private BankOneConfig bankOne;

        @NotNull
        @Valid
        private BankTwoConfig bankTwo;
    }

    @Getter
    @Setter
    public static class BankOneConfig {
        @NotNull
        @Valid
        private URL baseUrl;

        @NotNull
        @Valid
        private String accountLookupPath;

        @NotNull
        @Valid
        private Integer connectTimeoutMillis;

        @NotNull
        @Valid
        private Integer readTimeoutMillis;
    }

    @Getter
    @Setter
    public static class BankTwoConfig {
        @NotNull
        @Valid
        private URL baseUrl;

        @NotNull
        @Valid
        private String accountLookupPath;

        @NotNull
        @Valid
        private Integer connectTimeoutMillis;

        @NotNull
        @Valid
        private Integer readTimeoutMillis;
    }
}
