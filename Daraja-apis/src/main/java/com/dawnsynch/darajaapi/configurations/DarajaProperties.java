package com.dawnsynch.darajaapi.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mpesa.daraja")
public record DarajaProperties(
        String consumerKey,
        String consumerSecret,
        String passkey,
        String businessShortcode,
        String stkPushCallbackUrl,
        String stkPushUrl,
        String responseType,
        String shortCode,
        String confirmationUrl,
        String validationUrl,
        String registerUrlEndpoint,
        String simulateTransactionEndpoint,
        String b2cInitiatorPassword,
        String b2cResultUrl,
        String b2cQueueTimeoutUrl,
        String b2cInitiatorName,
        String b2cTransactionEndpoint,
        String transactionResultUrl,
        String checkAccountBalanceUrl,
        String securityCredential
) {}
