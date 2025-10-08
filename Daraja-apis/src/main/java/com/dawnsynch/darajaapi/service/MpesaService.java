package com.dawnsynch.darajaapi.service;

import com.dawnsynch.darajaapi.configurations.DarajaProperties;
import com.dawnsynch.darajaapi.dtos.*;
import com.dawnsynch.darajaapi.entity.B2C_C2B_Callback;
import com.dawnsynch.darajaapi.entity.STKCallbackLog;
import com.dawnsynch.darajaapi.exceptions.*;
import com.dawnsynch.darajaapi.records.*;
import com.dawnsynch.darajaapi.repository.B2C_C2B_CallbackRepository;
import com.dawnsynch.darajaapi.repository.STKPushLogRepository;
import com.dawnsynch.darajaapi.utils.Helper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dawnsynch.darajaapi.utils.Constants.*;

@Service
@RequiredArgsConstructor
public class MpesaService {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final STKPushLogRepository stkPushLogRepository;
    private final B2C_C2B_CallbackRepository b2cC2BCallbackRepository;
    private final DarajaProperties properties;


    //    THIS METHOD GENERATES ACCESS TOKEN TO BE USED FOR AUTHORIZATION BY OTHER DARAJA APIS
    @CircuitBreaker(name="daraja")
    @Retry(name="daraja")
    public AccessTokenResponse generateAccessToken() throws IOException {
        String credentials = Credentials.basic(properties.consumerKey(), properties.consumerSecret());

        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .get()
                .addHeader("Authorization", credentials)
                .build();


        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new MpesaAuthenticationException("Failed to generate access token: " + response.code());
            }
            return objectMapper.readValue(response.body().string(), AccessTokenResponse.class);

        } catch (JsonProcessingException e) {
            throw new MpesaResponseException("Invalid JSON response while fetching access token");
        } catch (IOException e) {
            throw new MpesaNetworkException("Network error while generating access token", e);
        } catch (Exception e) {
            throw new MpesaProcessingException("Unexpected error while generating access token", e);
        }
    }


//    INITIATES THE STK PUSH

    @CircuitBreaker(name="daraja")
    @Retry(name="daraja")
    public STKSyncResponse initiateSTKPush(String phoneNumber, String amount) {
        try {
            phoneNumber = phoneNumber.startsWith("0") ? phoneNumber.replaceFirst("0", "254") : phoneNumber;

            AccessTokenResponse token = generateAccessToken();
            String timestamp = Helper.getTimestamp();
            String password = Helper.toBase64(properties.businessShortcode() + properties.passkey() + timestamp);

            STKPushRequest requestBody = new STKPushRequest();
            requestBody.setBusinessShortCode(properties.businessShortcode());
            requestBody.setPassword(password);
            requestBody.setTimestamp(timestamp);
            requestBody.setTransactionType("CustomerPayBillOnline");
            requestBody.setAmount(amount);
            requestBody.setPartyA(phoneNumber);
            requestBody.setPartyB(properties.businessShortcode());
            requestBody.setPhoneNumber(phoneNumber);
            requestBody.setCallBackURL(properties.stkPushCallbackUrl());
            requestBody.setAccountReference("DawnSynch Tech");
            requestBody.setTransactionDesc(String.format("%s Transaction", phoneNumber));

            RequestBody body = RequestBody.create(
                    objectMapper.writeValueAsString(requestBody),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(properties.stkPushUrl())
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token.accessToken())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String err = response.body() != null ? response.body().string() : "No response body";
                    throw new MpesaResponseException("STK push failed: " + err);
                }
                return objectMapper.readValue(response.body().string(), STKSyncResponse.class);
            }

        } catch (JsonProcessingException e) {
            throw new MpesaRequestException("Invalid STK push request format");
        } catch (IOException e) {
            throw new MpesaNetworkException("Network error during STK push", e);
        } catch (MpesaException e) {
            throw e; // keep known errors
        } catch (Exception e) {
            throw new MpesaProcessingException("Unexpected error during STK push", e);
        }
    }



//    REGISTER URL FOR C2B TRANSACTION

    @CircuitBreaker(name = "daraja")
    @Retry(name = "daraja")
    public RegisterUrlResponse registerUrl() {
        try {
            AccessTokenResponse accessTokenResponse = generateAccessToken();

            RegisterUrlRequest registerUrlRequest = new RegisterUrlRequest();
            registerUrlRequest.setConfirmationURL(properties.confirmationUrl());
            registerUrlRequest.setResponseType(properties.responseType());
            registerUrlRequest.setValidationURL(properties.validationUrl());
            registerUrlRequest.setShortCode(properties.shortCode());

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"),
                    objectMapper.writeValueAsString(registerUrlRequest)
            );

            Request request = new Request.Builder()
                    .url(properties.registerUrlEndpoint())
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + accessTokenResponse.accessToken())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    throw new MpesaResponseException("Failed to register C2B URL. HTTP " + response.code() + ": " + errorBody);
                }

                return objectMapper.readValue(response.body().string(), RegisterUrlResponse.class);
            }

        } catch (JsonProcessingException e) {
            throw new MpesaRequestException("Error parsing or serializing C2B registration payload");
        } catch (IOException e) {
            throw new MpesaNetworkException("Network error while registering C2B URL", e);
        } catch (MpesaException e) {
            throw e; // rethrow known custom errors
        } catch (Exception e) {
            throw new MpesaProcessingException("Unexpected error during C2B URL registration", e);
        }
    }


    //    SIMULATE C2B TRANSACTION
    @CircuitBreaker(name = "daraja")
    @Retry(name = "daraja")
    public SimulateC2BResponse simulateC2BTransaction(SimulateC2BRequest simulateC2BRequest) {
        try {
            AccessTokenResponse accessTokenResponse = generateAccessToken();

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    objectMapper.writeValueAsString(simulateC2BRequest)
            );

            Request request = new Request.Builder()
                    .url(properties.simulateTransactionEndpoint())
                    .post(body)
                    .addHeader("Authorization", "Bearer " + accessTokenResponse.accessToken())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    throw new MpesaResponseException("Could not simulate C2B transaction. HTTP " + response.code() + ": " + errorBody);
                }

                return objectMapper.readValue(response.body().string(), SimulateC2BResponse.class);
            }

        } catch (JsonProcessingException e) {
            throw new MpesaRequestException("Error serializing C2B simulation request");
        } catch (IOException e) {
            throw new MpesaNetworkException("Network error during C2B transaction simulation", e);
        } catch (MpesaException e) {
            throw e;
        } catch (Exception e) {
            throw new MpesaProcessingException("Unexpected error during C2B simulation", e);
        }
    }



//      SIMULATE B2C TRANSACTION

    @CircuitBreaker(name = "daraja")
    @Retry(name = "daraja")
    public CommonSyncResponse performB2CTransaction(@NotNull InternalB2CTransactionRequest internalB2CTransactionRequest) {
        try {
            AccessTokenResponse accessTokenResponse = generateAccessToken();

            B2CTransactionRequest b2cTransactionRequest = new B2CTransactionRequest();
            b2cTransactionRequest.setCommandID(internalB2CTransactionRequest.getCommandID());
            b2cTransactionRequest.setAmount(String.valueOf(internalB2CTransactionRequest.getAmount()));
            b2cTransactionRequest.setPartyB(internalB2CTransactionRequest.getPartyB());
            b2cTransactionRequest.setRemarks(internalB2CTransactionRequest.getRemarks());
            b2cTransactionRequest.setOccassion(internalB2CTransactionRequest.getOccassion());
            b2cTransactionRequest.setSecurityCredential(properties.securityCredential());
            b2cTransactionRequest.setResultURL(properties.b2cResultUrl());
            b2cTransactionRequest.setQueueTimeOutURL(properties.b2cQueueTimeoutUrl());
            b2cTransactionRequest.setInitiatorName(properties.b2cInitiatorName());
            b2cTransactionRequest.setPartyA(properties.shortCode());

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    objectMapper.writeValueAsString(b2cTransactionRequest)
            );

            Request request = new Request.Builder()
                    .url(properties.b2cTransactionEndpoint())
                    .post(body)
                    .addHeader("Authorization", "Bearer " + accessTokenResponse.accessToken())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    throw new MpesaResponseException("Could not perform B2C transaction. HTTP " + response.code() + ": " + errorBody);
                }

                return objectMapper.readValue(response.body().string(), CommonSyncResponse.class);
            }

        } catch (JsonProcessingException e) {
            throw new MpesaRequestException("Error serializing B2C transaction request");
        } catch (IOException e) {
            throw new MpesaNetworkException("Network error during B2C transaction", e);
        } catch (MpesaException e) {
            throw e;
        } catch (Exception e) {
            throw new MpesaProcessingException("Unexpected error during B2C transaction", e);
        }
    }


//    QUERY TRANSACTION

    @CircuitBreaker(name = "daraja")
    @Retry(name = "daraja")
    public CommonSyncResponse getTransactionResult(InternalTransactionStatusRequest internalTransactionStatusRequest) {
        try {
            AccessTokenResponse accessTokenResponse = generateAccessToken();

            TransactionStatusRequest transactionStatusRequest = new TransactionStatusRequest();
            transactionStatusRequest.setTransactionID(internalTransactionStatusRequest.getTransactionID());
            transactionStatusRequest.setInitiator(properties.b2cInitiatorName());
            transactionStatusRequest.setSecurityCredential(properties.securityCredential());
            transactionStatusRequest.setCommandID(TRANSACTION_STATUS_QUERY_COMMAND);
            transactionStatusRequest.setPartyA(properties.shortCode());
            transactionStatusRequest.setIdentifierType(SHORT_CODE_IDENTIFIER);
            transactionStatusRequest.setResultURL(properties.b2cResultUrl());
            transactionStatusRequest.setQueueTimeOutURL(properties.b2cQueueTimeoutUrl());
            transactionStatusRequest.setRemarks(TRANSACTION_STATUS_VALUE);
            transactionStatusRequest.setOccasion(TRANSACTION_STATUS_VALUE);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    objectMapper.writeValueAsString(transactionStatusRequest)
            );

            Request request = new Request.Builder()
                    .url(properties.transactionResultUrl())
                    .post(body)
                    .addHeader("Authorization", "Bearer " + accessTokenResponse.accessToken())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    throw new MpesaResponseException("Failed to fetch transaction result: " + response.code() + " - " + errorBody);
                }
                return objectMapper.readValue(response.body().string(), CommonSyncResponse.class);
            }

        } catch (JsonProcessingException e) {
            throw new MpesaRequestException("Invalid JSON response while processing transaction result: " + e.getMessage());
        } catch (MpesaException e) {
            throw e;
        } catch (IOException e) {
            throw new MpesaNetworkException("Network error while fetching transaction result: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new MpesaProcessingException("Unexpected error while fetching transaction result: " + e.getMessage(), e);
        }
    }



//    CHECK ACCOUNT BALANCE

    @CircuitBreaker(name="daraja")
    @Retry(name="daraja")
    public CommonSyncResponse checkAccountBalance() {
        try {
            AccessTokenResponse accessTokenResponse = generateAccessToken();

            CheckAccountBalanceRequest checkAccountBalanceRequest = new CheckAccountBalanceRequest();
            checkAccountBalanceRequest.setInitiator(properties.b2cInitiatorName());
            checkAccountBalanceRequest.setSecurityCredential(properties.securityCredential());
            checkAccountBalanceRequest.setCommandID(ACCOUNT_BALANCE_COMMAND);
            checkAccountBalanceRequest.setPartyA(properties.shortCode());
            checkAccountBalanceRequest.setIdentifierType(SHORT_CODE_IDENTIFIER);
            checkAccountBalanceRequest.setRemarks("Check account balance");
            checkAccountBalanceRequest.setQueueTimeOutURL(properties.b2cQueueTimeoutUrl());
            checkAccountBalanceRequest.setResultURL(properties.b2cResultUrl());

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    objectMapper.writeValueAsString(checkAccountBalanceRequest)
            );

            Request request = new Request.Builder()
                    .url(properties.checkAccountBalanceUrl())
                    .post(body)
                    .addHeader("Authorization", "Bearer " + accessTokenResponse.accessToken())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    throw new MpesaResponseException("Failed to check account balance: " + response.code() + " - " + errorBody);
                }
                return objectMapper.readValue(response.body().string(), CommonSyncResponse.class);

            }

        } catch (JsonProcessingException e) {
            throw new MpesaRequestException("Invalid JSON response while checking account balance: " + e.getMessage());
        } catch (MpesaException e) {
            throw e;
        } catch (IOException e) {
            throw new MpesaNetworkException("Network error while checking account balance: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new MpesaProcessingException("Unexpected error while checking account balance: " + e.getMessage(), e);
        }
    }



//    SAVING C2B AND B2C TRANSACTION RESULTS TO DATABASE

    public void saveCallback(TransactionAsyncResponse response) {
        TransactionAsyncResponse.Result result = response.result();

        B2C_C2B_Callback entity = new B2C_C2B_Callback();
        entity.setResultType(result.resultType());
        entity.setResultCode(result.resultCode());
        entity.setResultDesc(result.resultDesc());
        entity.setOriginatorConversationId(result.originatorConversationID());
        entity.setConversationId(result.conversationID());
        entity.setTransactionId(result.transactionID());

        // ReferenceData
        if (result.referenceData() != null) {
            entity.setReferenceItemKey(result.referenceData().referenceItem().key());
            entity.setReferenceItemValue(result.referenceData().referenceItem().value());
        }

        // ResultParameters (only present if success)
        Map<String, String> params = (result.resultParameters() != null
                && result.resultParameters().resultParameter() != null)
                ? result.resultParameters().resultParameter().stream()
                .collect(Collectors.toMap(
                        TransactionAsyncResponse.ResultParameter::key,
                        TransactionAsyncResponse.ResultParameter::value
                ))
                : Collections.emptyMap();

        if (!params.isEmpty()) {
            entity.setTransactionAmount(params.containsKey("TransactionAmount")
                    ? Double.valueOf(params.get("TransactionAmount")) : null);

            entity.setTransactionReceipt(params.get("TransactionReceipt"));
            entity.setReceiverPartyPublicName(params.get("ReceiverPartyPublicName"));

            if (params.containsKey("TransactionCompletedDateTime")) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                    entity.setTransactionCompletedDatetime(
                            LocalDateTime.parse(params.get("TransactionCompletedDateTime"), formatter)
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            entity.setB2cUtilityAccountFunds(params.containsKey("B2CUtilityAccountAvailableFunds")
                    ? Double.valueOf(params.get("B2CUtilityAccountAvailableFunds")) : null);

            entity.setB2cWorkingAccountFunds(params.containsKey("B2CWorkingAccountAvailableFunds")
                    ? Double.valueOf(params.get("B2CWorkingAccountAvailableFunds")) : null);

            entity.setB2cRecipientRegisteredCustomer(params.get("B2CRecipientIsRegisteredCustomer"));

            entity.setB2cChargesPaidAccountFunds(params.containsKey("B2CChargesPaidAccountAvailableFunds")
                    ? Double.valueOf(params.get("B2CChargesPaidAccountAvailableFunds")) : null);
        }

        b2cC2BCallbackRepository.save(entity);
    }

    public void processStkCallback(STKCallback callback) {
        STKCallback.StkCallback stk = callback.body().stkCallback();

        Double amount = null;
        String receipt = null;
        String txnDate = null;
        String phone = null;

        if (stk.resultCode() == 0 && stk.callbackMetadata() != null) {
            for (STKCallback.Item item : stk.callbackMetadata().item()) {
                switch (item.name()) {
                    case "Amount" -> amount = Double.valueOf(item.value().toString());
                    case "MpesaReceiptNumber" -> receipt = item.value().toString();
                    case "TransactionDate" -> txnDate = item.value().toString();
                    case "PhoneNumber" -> phone = item.value().toString();
                }
            }
        }

        STKCallbackLog log = STKCallbackLog.builder()
                .merchantRequestId(stk.merchantRequestID())
                .checkoutRequestId(stk.checkoutRequestID())
                .resultCode(stk.resultCode())
                .resultDesc(stk.resultDesc())
                .amount(amount)   // null if failed
                .mpesaReceiptNumber(receipt)
                .transactionDate(txnDate)
                .phoneNumber(phone)
                .build();

        stkPushLogRepository.save(log);
    }
}




