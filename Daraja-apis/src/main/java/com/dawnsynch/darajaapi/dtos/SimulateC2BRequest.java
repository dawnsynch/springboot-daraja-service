package com.dawnsynch.darajaapi.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.*;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulateC2BRequest {

    @NotBlank(message = "ShortCode is required")
    @Pattern(regexp = "^[0-9]{5,7}$", message = "ShortCode must be 5–7 digits")
    @JsonProperty("ShortCode")
    private String shortCode;

    @NotBlank(message = "Msisdn is required")
    @JsonProperty("Msisdn")
    private String msisdn;

    @NotBlank(message = "BillRefNumber is required")
    @Size(max = 20, message = "BillRefNumber must not exceed 20 characters")
    @JsonProperty("BillRefNumber")
    private String billRefNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    @JsonProperty("Amount")
    private Double amount;

    @NotBlank(message = "CommandID is required")
    @Pattern(
            regexp = "^(CustomerPayBillOnline|CustomerBuyGoodsOnline)$",
            message = "CommandID must be either CustomerPayBillOnline or CustomerBuyGoodsOnline"
    )
    @JsonProperty("CommandID")
    private String commandID;
}
