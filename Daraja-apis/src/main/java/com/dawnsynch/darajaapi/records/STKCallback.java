package com.dawnsynch.darajaapi.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record STKCallback(
        @JsonProperty("Body") Body body
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(
            @JsonProperty("stkCallback") StkCallback stkCallback
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StkCallback(
            @JsonProperty("MerchantRequestID") String merchantRequestID,
            @JsonProperty("CheckoutRequestID") String checkoutRequestID,
            @JsonProperty("ResultCode") int resultCode,
            @JsonProperty("ResultDesc") String resultDesc,
            @JsonProperty("CallbackMetadata") CallbackMetadata callbackMetadata
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CallbackMetadata(
            @JsonProperty("Item") List<Item> item
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            @JsonProperty("Name") String name,
            @JsonProperty("Value") Object value
    ) {}
}
