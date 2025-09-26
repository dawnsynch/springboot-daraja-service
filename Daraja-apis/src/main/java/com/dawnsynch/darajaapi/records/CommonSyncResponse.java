package com.dawnsynch.darajaapi.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// FOR B2C AND TRANSACTION STATUS SYNC RESPONSE

@JsonIgnoreProperties(ignoreUnknown = true)
public record CommonSyncResponse(
        @JsonProperty("ConversationID") String conversationID,
        @JsonProperty("ResponseCode") String responseCode,
        @JsonProperty("OriginatorConversationID") String originatorConversationID,
        @JsonProperty("ResponseDescription") String responseDescription
) {}
