package com.dawnsynch.darajaapi.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionStatusAsyncResponse {

    @JsonProperty("Result")
    private Result result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        @JsonProperty("ResultType")
        private Integer resultType;

        @JsonProperty("ResultCode")
        private Integer resultCode;

        @JsonProperty("ResultDesc")
        private String resultDesc;

        @JsonProperty("OriginatorConversationID")
        private String originatorConversationID;

        @JsonProperty("ConversationID")
        private String conversationID;

        @JsonProperty("TransactionID")
        private String transactionID;

        @JsonProperty("ResultParameters")
        private ResultParameters resultParameters;

        @JsonProperty("ReferenceData")
        private ReferenceData referenceData;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultParameters {
        @JsonProperty("ResultParameter")
        private List<ResultParameter> resultParameter;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResultParameter {
        @JsonProperty("Key")
        private String key;

        @JsonProperty("Value")
        private String value;  // keep as String, you can cast later
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReferenceData {
        @JsonProperty("ReferenceItem")
        private ReferenceItem referenceItem;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReferenceItem {
        @JsonProperty("Key")
        private String key;

        @JsonProperty("Value")
        private String value;
    }
}
