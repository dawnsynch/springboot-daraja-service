package com.dawnsynch.darajaapi.service;

import com.dawnsynch.darajaapi.dtos.*;
import com.dawnsynch.darajaapi.entity.B2C_C2B_Callback;
import com.dawnsynch.darajaapi.entity.STKCallbackLog;
import com.dawnsynch.darajaapi.records.*;
import com.dawnsynch.darajaapi.repository.B2C_C2B_CallbackRepository;
import com.dawnsynch.darajaapi.repository.STKPushLogRepository;
import com.dawnsynch.darajaapi.utils.Helper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${mpesa.daraja.consumer-key}")
    private String consumerKey;

    @Value("${mpesa.daraja.consumer-secret}")
    private String consumerSecret;

    @Value("${mpesa.daraja.stk-push-url}")
    private String stkPushurl;

    @Value("${mpesa.daraja.stk-push-callback-url}")
    private String stkPushCallbackUrl;

    @Value("${mpesa.daraja.business-shortcode}")
    private String businessShortCode;

    @Value("${mpesa.daraja.passkey}")
    private String passkey;

    //    shortcode used for registering url
    @Value("${mpesa.daraja.shortCode}")
    private String shortCode;

    @Value("${mpesa.daraja.responseType}")
    private String responseType;

    @Value("${mpesa.daraja.confirmation-url}")
    private String confirmationUrl;

    @Value("${mpesa.daraja.validation-url}")
    private String validationUrl;

    @Value("${mpesa.daraja.register-url-endpoint}")
    private String registerUrlEndpoint;

    @Value("${mpesa.daraja.simulate-transaction-endpoint}")
    private String simulateTransactionEndpoint;

    @Value("${mpesa.daraja.b2c-initiator-password}")
    private String b2cInitiatorPassword;

    @Value("${mpesa.daraja.b2c-result-url}")
    private String b2cResultUrl;

    @Value("${mpesa.daraja.b2c-queue-timeout-url}")
    private String b2cQueueTimeoutUrl;

    @Value("${mpesa.daraja.b2c-initiator-name}")
    private String b2cInitiatorName;

    @Value("${mpesa.daraja.b2c-transaction-endpoint}")
    private String b2cTransactionEndpoint;

    @Value("${mpesa.daraja.transactionResultUrl}")
    private String transactionResultUrl;

    @Value("${mpesa.daraja.security-credential}")
    private String securityCredential;

    @Value("${mpesa.daraja.checkAccountBalanceUrl}")
    private String accountBalanceUrl;



    //    THIS METHOD GENERATES ACCESS TOKEN TO BE USED FOR AUTHORIZATION BY OTHER DARAJA APIS
    public AccessTokenResponse generateAccessToken() throws IOException {
        String credentials = Credentials.basic(consumerKey, consumerSecret);

        Request request = new Request.Builder()
                .url("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                .get()
                .addHeader("Authorization", credentials)
                .build();


        try (Response response = okHttpClient.newCall(request).execute()) {
//            Checks if the request was successful otherwise throw an error
            if (!response.isSuccessful()) {
                throw new IOException("Failed to generate access token");
            }
            return objectMapper.readValue(response.body().string(), AccessTokenResponse.class);
        }
    }


//    INITIATES THE STK PUSH

    public STKSyncResponse initiateSTKPush(String phoneNumber, String amount) throws IOException {
        phoneNumber = phoneNumber.startsWith("0") ? phoneNumber.replaceFirst("0", "254") : phoneNumber;

//         get access token here
        AccessTokenResponse accessTokenResponse = generateAccessToken();

        String timestamp = Helper.getTimestamp();
        String password = Helper.toBase64(businessShortCode + passkey + timestamp);

//        map values to stk push request inorder to send a json request
        STKPushRequest stkPushRequest = new STKPushRequest();
        stkPushRequest.setBusinessShortCode(businessShortCode);
        stkPushRequest.setPassword(password);
        stkPushRequest.setTimestamp(timestamp);
        stkPushRequest.setTransactionType("CustomerPayBillOnline");
        stkPushRequest.setAmount(amount);
        stkPushRequest.setPartyA(phoneNumber);
        stkPushRequest.setPartyB(businessShortCode);
        stkPushRequest.setPhoneNumber(phoneNumber);
        stkPushRequest.setCallBackURL(stkPushCallbackUrl);
        stkPushRequest.setAccountReference("DawnSynch Tech");
        stkPushRequest.setTransactionDesc(String.format("%s Transaction", phoneNumber));

        String jsonRequest = objectMapper.writeValueAsString(stkPushRequest);


        RequestBody requestBody = RequestBody.create(jsonRequest, MediaType.parse("application/json"));
//    sending request to daraja
        Request request = new Request.Builder()
                .url(stkPushurl)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + accessTokenResponse.accessToken())
                .addHeader("Content-Type", "application/json")
                .build();


        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to initiate STK push");
            }
            return objectMapper.readValue(response.body().string(), STKSyncResponse.class);
        }
    }


//    REGISTER URL FOR C2B TRANSACTION

    public RegisterUrlResponse registerUrl() throws IOException {
        AccessTokenResponse accessTokenResponse = generateAccessToken();

        RegisterUrlRequest  registerUrlRequest = new RegisterUrlRequest();
        registerUrlRequest.setConfirmationURL(confirmationUrl);
        registerUrlRequest.setResponseType(responseType);
        registerUrlRequest.setValidationURL(validationUrl);
        registerUrlRequest.setShortCode(shortCode);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), objectMapper.writeValueAsString(registerUrlRequest));


        Request request = new Request.Builder()
                .url(registerUrlEndpoint)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + accessTokenResponse.accessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                throw new IOException("Failed to register C2B URL. HTTP " + response.code() + ": " + errorBody);
            }

            return objectMapper.readValue(response.body().string(), RegisterUrlResponse.class);
        }
    }

//    SIMULATE C2B TRANSACTION
    public SimulateC2BResponse simulateC2BTransaction(SimulateC2BRequest simulateC2BRequest) throws IOException {
        AccessTokenResponse accessTokenResponse = generateAccessToken();

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), objectMapper.writeValueAsString(simulateC2BRequest));

        Request request = new Request.Builder()
                .url(simulateTransactionEndpoint)
                .post(body)
                .addHeader("Authorization", "Bearer " + accessTokenResponse.accessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                throw new IOException("Could not simulate C2B transactions " + response.code() + ": " + errorBody);
            }

            return objectMapper.readValue(response.body().string(), SimulateC2BResponse.class);
        }
    }


//      SIMULATE B2C TRANSACTION

    public CommonSyncResponse performB2CTransaction (@NotNull InternalB2CTransactionRequest internalB2CTransactionRequest) throws Exception {

        AccessTokenResponse accessTokenResponse = generateAccessToken();

        B2CTransactionRequest b2cTransactionRequest = new B2CTransactionRequest();
        b2cTransactionRequest.setCommandID(internalB2CTransactionRequest.getCommandID());
        b2cTransactionRequest.setAmount(internalB2CTransactionRequest.getAmount());
        b2cTransactionRequest.setPartyB(internalB2CTransactionRequest.getPartyB());
        b2cTransactionRequest.setRemarks(internalB2CTransactionRequest.getRemarks());
        b2cTransactionRequest.setOccassion(internalB2CTransactionRequest.getOccassion());

//        Get security credentials
        b2cTransactionRequest.setSecurityCredential(securityCredential);
//        Set the result url
        b2cTransactionRequest.setResultURL(b2cResultUrl);
        b2cTransactionRequest.setQueueTimeOutURL(b2cQueueTimeoutUrl);
        b2cTransactionRequest.setInitiatorName(b2cInitiatorName);
        b2cTransactionRequest.setPartyA(shortCode);


        RequestBody body = RequestBody.create(MediaType.parse("application/json"), objectMapper.writeValueAsString(b2cTransactionRequest));

        Request request = new Request.Builder()
                .url(b2cTransactionEndpoint)
                .post(body)
                .addHeader("Authorization", "Bearer " + accessTokenResponse.accessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                throw new IOException("Could not perform B2C transaction " + response.code() + ": " + errorBody);
            }

            return objectMapper.readValue(response.body().string(), CommonSyncResponse.class);
        }

    }

//    QUERY TRANSACTION

    public CommonSyncResponse getTransactionResult(InternalTransactionStatusRequest internalTransactionStatusRequest) throws IOException {
        AccessTokenResponse accessTokenResponse = generateAccessToken();

        TransactionStatusRequest transactionStatusRequest = new TransactionStatusRequest();
        transactionStatusRequest.setTransactionID(internalTransactionStatusRequest.getTransactionID());
        transactionStatusRequest.setInitiator(b2cInitiatorName);
        transactionStatusRequest.setSecurityCredential(securityCredential);
        transactionStatusRequest.setCommandID(TRANSACTION_STATUS_QUERY_COMMAND);
        transactionStatusRequest.setPartyA(shortCode);
        transactionStatusRequest.setIdentifierType(SHORT_CODE_IDENTIFIER);
        transactionStatusRequest.setResultURL(b2cResultUrl);
        transactionStatusRequest.setQueueTimeOutURL(b2cQueueTimeoutUrl);
        transactionStatusRequest.setRemarks(TRANSACTION_STATUS_VALUE);
        transactionStatusRequest.setOccasion(TRANSACTION_STATUS_VALUE);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), objectMapper.writeValueAsString(transactionStatusRequest));

        Request request = new Request.Builder()
                .url(transactionResultUrl)
                .post(body)
                .addHeader("Authorization", "Bearer " + accessTokenResponse.accessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                throw new IOException("Could not fetch transaction result " + response.code() + ": " + errorBody);
            }

            return objectMapper.readValue(response.body().string(), CommonSyncResponse.class);
        }
    }


//    CHECK ACCOUNT BALANCE

    public CommonSyncResponse checkAccountBalance() throws IOException {
        AccessTokenResponse accessTokenResponse = generateAccessToken();

        CheckAccountBalanceRequest checkAccountBalanceRequest = new CheckAccountBalanceRequest();
        checkAccountBalanceRequest.setInitiator(b2cInitiatorName);
        checkAccountBalanceRequest.setSecurityCredential(securityCredential);
        checkAccountBalanceRequest.setCommandID(ACCOUNT_BALANCE_COMMAND);
        checkAccountBalanceRequest.setPartyA(shortCode);
        checkAccountBalanceRequest.setIdentifierType(SHORT_CODE_IDENTIFIER);
        checkAccountBalanceRequest.setRemarks("Check account balance");
        checkAccountBalanceRequest.setQueueTimeOutURL(b2cQueueTimeoutUrl);
        checkAccountBalanceRequest.setResultURL(b2cResultUrl);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), objectMapper.writeValueAsString(checkAccountBalanceRequest));

        Request request = new Request.Builder()
                .url(accountBalanceUrl)
                .post(body)
                .addHeader("Authorization", "Bearer " + accessTokenResponse.accessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                throw new IOException("Could not fetch the account balance " + response.code() + ": " + errorBody);
            }

            return objectMapper.readValue(response.body().string(), CommonSyncResponse.class);
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




