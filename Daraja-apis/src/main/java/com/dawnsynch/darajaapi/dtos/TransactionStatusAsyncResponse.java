package com.dawnsynch.darajaapi.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionStatusAsyncResponse {

    @JsonProperty("Result")
    private Result result;
}
