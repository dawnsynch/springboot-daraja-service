package com.dawnsynch.darajaapi.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InternalTransactionStatusRequest {

    @NotBlank(message = "TransactionID is required")
    @Pattern(
            regexp = "^[A-Za-z0-9-]{10,30}$",
            message = "TransactionID must be alphanumeric (10–30 characters)"
    )
    @JsonProperty("TransactionID")
    private String transactionID;
}
