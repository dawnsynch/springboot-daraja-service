package com.dawnsynch.darajaapi.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InternalB2CTransactionRequest {

    @NotBlank(message = "Remarks are required")
    @Size(max = 50, message = "Remarks must not exceed 50 characters")
    @JsonProperty("Remarks")
    private String remarks;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    @JsonProperty("Amount")
    private Double amount;

    @Size(max = 50, message = "Occasion must not exceed 50 characters")
    @JsonProperty("Occassion")
    private String occassion;

    @NotBlank(message = "CommandID is required")
    @Pattern(
            regexp = "^(BusinessPayment|SalaryPayment|PromotionPayment)$",
            message = "CommandID must be one of BusinessPayment, SalaryPayment, PromotionPayment"
    )
    @JsonProperty("CommandID")
    private String commandID;

    @NotBlank(message = "PartyB is required")
    @JsonProperty("PartyB")
    private String partyB;
}
