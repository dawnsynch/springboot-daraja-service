package com.dawnsynch.darajaapi.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class STKPushRequest {

    @NotBlank(message = "BusinessShortCode is required")
    @Pattern(regexp = "^[0-9]{5,7}$", message = "BusinessShortCode must be 5–7 digits")
    @JsonProperty("BusinessShortCode")
    private String businessShortCode;

    @NotBlank(message = "Password is required")
    @JsonProperty("Password")
    private String password;

    @NotBlank(message = "Timestamp is required")
    @Pattern(regexp = "^[0-9]{14}$", message = "Timestamp must be in format yyyyMMddHHmmss")
    @JsonProperty("Timestamp")
    private String timestamp;

    @NotBlank(message = "TransactionType is required")
    @JsonProperty("TransactionType")
    private String transactionType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    @JsonProperty("Amount")
    private String amount;

    @NotBlank(message = "PartyA is required")
    @Pattern(regexp = "^254[7-9][0-9]{7}$", message = "PartyA must be a valid MSISDN starting with 2547/8/9")
    @JsonProperty("PartyA")
    private String partyA;

    @NotBlank(message = "PartyB is required")
    @Pattern(regexp = "^[0-9]{5,7}$", message = "PartyB must be a valid paybill or till number")
    @JsonProperty("PartyB")
    private String partyB;

    @NotBlank(message = "PhoneNumber is required")
    @Pattern(regexp = "^254[7-9][0-9]{7}$", message = "PhoneNumber must be a valid MSISDN starting with 2547/8/9")
    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @NotBlank(message = "CallBackURL is required")
    @Pattern(
            regexp = "^(https?)://.*$",
            message = "CallBackURL must be a valid URL starting with http or https"
    )
    @JsonProperty("CallBackURL")
    private String callBackURL;

    @NotBlank(message = "AccountReference is required")
    @Size(max = 20, message = "AccountReference must not exceed 20 characters")
    @JsonProperty("AccountReference")
    private String accountReference;

    @NotBlank(message = "TransactionDesc is required")
    @Size(max = 50, message = "TransactionDesc must not exceed 50 characters")
    @JsonProperty("TransactionDesc")
    private String transactionDesc;
}
