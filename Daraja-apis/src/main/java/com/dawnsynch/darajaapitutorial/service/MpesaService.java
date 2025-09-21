package com.dawnsynch.darajaapitutorial.service;

import com.dawnsynch.darajaapitutorial.dtos.*;
import com.dawnsynch.darajaapitutorial.entity.STKCallbackLog;
import com.dawnsynch.darajaapitutorial.repository.STKPushLogRepository;
import com.dawnsynch.darajaapitutorial.utils.Helper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MpesaService {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final STKPushLogRepository stkPushLogRepository;

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

    public STKPushResponse initiateSTKPush(String phoneNumber, String amount) throws IOException {
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
                .addHeader("Authorization", "Bearer " + accessTokenResponse.getAccess_token())
                .addHeader("Content-Type", "application/json")
                .build();


        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to initiate STK push");
            }
            return objectMapper.readValue(response.body().string(), STKPushResponse.class);
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
                .addHeader("Authorization", "Bearer " + accessTokenResponse.getAccess_token())
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

//    SIMULATE C2B Transaction
    public SimulateC2BResponse simulateC2BTransaction(SimulateC2BRequest simulateC2BRequest) throws IOException {
        AccessTokenResponse accessTokenResponse = generateAccessToken();

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), objectMapper.writeValueAsString(simulateC2BRequest));

        Request request = new Request.Builder()
                .url(simulateTransactionEndpoint)
                .post(body)
                .addHeader("Authorization", "Bearer " + accessTokenResponse.getAccess_token())
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

}

