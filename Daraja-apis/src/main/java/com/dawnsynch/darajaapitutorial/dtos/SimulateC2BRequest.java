package com.dawnsynch.darajaapitutorial.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulateC2BRequest {

    @JsonProperty("ShortCode")
    private String shortCode;
    @JsonProperty("Msisdn")
    private String msisdn;
    @JsonProperty("BIllRefNumber")
    private String billRefNumber;
    @JsonProperty("Amount")
    private String amount;
    @JsonProperty("CommandID")
    private String commandID;
}
