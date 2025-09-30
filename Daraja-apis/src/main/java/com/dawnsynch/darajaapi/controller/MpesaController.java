package com.dawnsynch.darajaapi.controller;

import com.dawnsynch.darajaapi.dtos.*;
import com.dawnsynch.darajaapi.records.*;
import com.dawnsynch.darajaapi.repository.STKPushLogRepository;
import com.dawnsynch.darajaapi.service.MpesaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("api/payments")
@Slf4j
@RequiredArgsConstructor
public class MpesaController {

    private final MpesaService  mpesaService;
    private final STKPushLogRepository stkPushLogRepository;
    private final AcknowledgeResponse acknowledgeResponse;


//    Get access Token
    @GetMapping("/access-token")
    public ResponseEntity<AccessTokenResponse> getAccessToken() {
        try {
            return ResponseEntity.ok(mpesaService.generateAccessToken());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    INITIATE STK PUSH
    @PostMapping("/stk-push")
    public ResponseEntity<STKSyncResponse> stkPush(@RequestBody Map<String, String> payload) {
        try {
            return ResponseEntity.ok(mpesaService.initiateSTKPush(payload.get("phoneNumber"), payload.get("amount")));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    STK CALLBACK
@PostMapping("/callback")
public Map<String, String> handleCallback(@Valid @RequestBody STKCallback callback) {
    mpesaService.processStkCallback(callback);

    return Map.of(
            "ResultCode", "0",
            "ResultDesc", "Callback received successfully"
    );
}


//      REGISTER URL FOR C2B
    @PostMapping("/register-url")
    public ResponseEntity<?> registerUrl() {
        try {
            return ResponseEntity.ok(mpesaService.registerUrl());
        } catch (Exception e) {
            e.printStackTrace(); // log in console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred: " + e.getMessage());
        }
    }

//    VALIDATION
    @PostMapping("/validation")
    public ResponseEntity<AcknowledgeResponse> validateTransaction(@Valid @RequestBody TransactionResult transactionResult){
        return ResponseEntity.ok(acknowledgeResponse);
    }


//    SIMULATE C2B TRANSACTION
    @PostMapping("/simulate-c2b")
    public ResponseEntity<SimulateC2BResponse> simulateC2BTransaction(@Valid @RequestBody SimulateC2BRequest simulateC2BRequest) throws IOException {
        try {
            return ResponseEntity.ok(mpesaService.simulateC2BTransaction(simulateC2BRequest));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    SIMULATE B2C TRANSACTION
    @PostMapping("/b2c-transaction")
    public ResponseEntity<CommonSyncResponse> performB2CTransaction(@Valid @RequestBody InternalB2CTransactionRequest internalB2CTransactionRequest) throws Exception {
        return ResponseEntity.ok(mpesaService.performB2CTransaction(internalB2CTransactionRequest));

    }

    @PostMapping("/b2c-transaction-result")
    public ResponseEntity<AcknowledgeResponse> b2cTransactionAsyncResults(@Valid @RequestBody TransactionAsyncResponse transactionAsyncResponse){
        mpesaService.saveCallback(transactionAsyncResponse);
        return ResponseEntity.ok(acknowledgeResponse);
    }

    @PostMapping(path = "/b2c-queue-timeout", produces = "application/json")
    public ResponseEntity<AcknowledgeResponse> queueTimeout(@RequestBody Object object) {
        return ResponseEntity.ok(acknowledgeResponse);
    }


    @PostMapping(path = "/simulate-transaction-result", produces = "application/json")
    public ResponseEntity<CommonSyncResponse> getTransactionStatusResult(@Valid @RequestBody InternalTransactionStatusRequest internalTransactionStatusRequest){
            try {
                return ResponseEntity.ok(mpesaService.getTransactionResult(internalTransactionStatusRequest));
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

    @GetMapping("/check-account-balance")
    public ResponseEntity<CommonSyncResponse> checkAccountBalance() throws IOException {
        return ResponseEntity.ok(mpesaService.checkAccountBalance());
    }
}
