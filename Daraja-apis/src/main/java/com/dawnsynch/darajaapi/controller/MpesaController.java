package com.dawnsynch.darajaapi.controller;

import com.dawnsynch.darajaapi.dtos.*;
import com.dawnsynch.darajaapi.records.*;
import com.dawnsynch.darajaapi.repository.STKPushLogRepository;
import com.dawnsynch.darajaapi.service.MpesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("api/payments")
@Slf4j
@RequiredArgsConstructor
public class MpesaController {

    private final MpesaService mpesaService;
    private final STKPushLogRepository stkPushLogRepository;
    private final AcknowledgeResponse acknowledgeResponse;

    // Get Access Token
    @GetMapping("/access-token")
    public ResponseEntity<AccessTokenResponse> getAccessToken() throws IOException {
        return ResponseEntity.ok(mpesaService.generateAccessToken());
    }

    //  Initiate STK Push
    @PostMapping("/stk-push")
    public ResponseEntity<STKSyncResponse> stkPush(@RequestBody Map<String, String> payload) throws IOException {
        return ResponseEntity.ok(
                mpesaService.initiateSTKPush(payload.get("phoneNumber"), payload.get("amount"))
        );
    }

    //  STK Callback
    @PostMapping("/callback")
    public Map<String, String> handleCallback(@Valid @RequestBody STKCallback callback) {
        mpesaService.processStkCallback(callback);
        return Map.of(
                "ResultCode", "0",
                "ResultDesc", "Callback received successfully"
        );
    }

    //  Register C2B URL
    @PostMapping("/register-url")
    public ResponseEntity<RegisterUrlResponse> registerUrl() throws IOException {
        return ResponseEntity.ok(mpesaService.registerUrl());
    }

    //  Validation
    @PostMapping("/validation")
    public ResponseEntity<AcknowledgeResponse> validateTransaction(
            @Valid @RequestBody TransactionResult transactionResult) {
        return ResponseEntity.ok(acknowledgeResponse);
    }

    //  Simulate C2B Transaction
    @PostMapping("/simulate-c2b")
    public ResponseEntity<SimulateC2BResponse> simulateC2BTransaction(
            @Valid @RequestBody SimulateC2BRequest simulateC2BRequest) throws IOException {
        return ResponseEntity.ok(mpesaService.simulateC2BTransaction(simulateC2BRequest));
    }

    //  Perform B2C Transaction
    @PostMapping("/b2c-transaction")
    public ResponseEntity<CommonSyncResponse> performB2CTransaction(
            @Valid @RequestBody InternalB2CTransactionRequest internalB2CTransactionRequest) throws Exception {
        return ResponseEntity.ok(mpesaService.performB2CTransaction(internalB2CTransactionRequest));
    }

    //  B2C Transaction Async Results
    @PostMapping("/b2c-transaction-result")
    public ResponseEntity<AcknowledgeResponse> b2cTransactionAsyncResults(
            @Valid @RequestBody TransactionAsyncResponse transactionAsyncResponse) {
        mpesaService.saveCallback(transactionAsyncResponse);
        return ResponseEntity.ok(acknowledgeResponse);
    }

    //  B2C Queue Timeout
    @PostMapping(path = "/b2c-queue-timeout", produces = "application/json")
    public ResponseEntity<AcknowledgeResponse> queueTimeout(@RequestBody Object object) {
        return ResponseEntity.ok(acknowledgeResponse);
    }

    //  Query Transaction Result
    @PostMapping(path = "/simulate-transaction-result", produces = "application/json")
    public ResponseEntity<CommonSyncResponse> getTransactionStatusResult(
            @Valid @RequestBody InternalTransactionStatusRequest internalTransactionStatusRequest) throws IOException {
        return ResponseEntity.ok(mpesaService.getTransactionResult(internalTransactionStatusRequest));
    }

    //  Check Account Balance
    @GetMapping("/check-account-balance")
    public ResponseEntity<CommonSyncResponse> checkAccountBalance() throws IOException {
        return ResponseEntity.ok(mpesaService.checkAccountBalance());
    }
}
