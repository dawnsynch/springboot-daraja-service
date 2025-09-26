package com.dawnsynch.darajaapi.records;


import com.fasterxml.jackson.annotation.JsonProperty;

public record STKSyncResponse(
        @JsonProperty("MerchantRequestID") String merchantRequestID,
        @JsonProperty("CheckoutRequestID") String checkoutRequestID,
        @JsonProperty("ResponseCode") String responseCode,
        @JsonProperty("ResponseDescription") String responseDescription,
        @JsonProperty("CustomerMessage") String customerMessage
) {}

