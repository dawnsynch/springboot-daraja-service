package com.dawnsynch.darajaapi.records;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransactionAsyncResponse(
        @JsonProperty("Result") Result result
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            @JsonProperty("ResultType") Integer resultType,
            @JsonProperty("ResultCode") Integer resultCode,
            @JsonProperty("ResultDesc") String resultDesc,
            @JsonProperty("OriginatorConversationID") String originatorConversationID,
            @JsonProperty("ConversationID") String conversationID,
            @JsonProperty("TransactionID") String transactionID,
            @JsonProperty("ResultParameters") ResultParameters resultParameters,
            @JsonProperty("ReferenceData") ReferenceData referenceData
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResultParameters(
            @JsonProperty("ResultParameter") List<ResultParameter> resultParameter
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResultParameter(
            @JsonProperty("Key") String key,
            @JsonProperty("Value") String value
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReferenceData(
            @JsonProperty("ReferenceItem") ReferenceItem referenceItem
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReferenceItem(
            @JsonProperty("Key") String key,
            @JsonProperty("Value") String value
    ) {}
}
