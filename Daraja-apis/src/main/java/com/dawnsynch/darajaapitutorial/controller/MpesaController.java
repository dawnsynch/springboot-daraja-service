package com.dawnsynch.darajaapitutorial.controller;

import com.dawnsynch.darajaapitutorial.dtos.*;
import com.dawnsynch.darajaapitutorial.entity.STKCallbackLog;
import com.dawnsynch.darajaapitutorial.repository.STKPushLogRepository;
import com.dawnsynch.darajaapitutorial.service.MpesaService;
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
    public ResponseEntity<STKPushResponse> stkPush(@RequestBody Map<String, String> payload) {
        try {
            return ResponseEntity.ok(mpesaService.initiateSTKPush(payload.get("phoneNumber"), payload.get("amount")));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    STK CALLBACK
    @PostMapping("/callback")
    public Map<String, String> handleCallback(@RequestBody STKCallbackDTO callbackDTO) {
        STKCallbackDTO.StkCallback stk = callbackDTO.getBody().getStkCallback();

        Double amount = null;
        String receipt = null;
        String txnDate = null;
        String phone = null;

        if (stk.getResultCode() == 0 && stk.getCallbackMetadata() != null) {
            // Successful transaction → parse metadata
            for (STKCallbackDTO.Item item : stk.getCallbackMetadata().getItem()) {
                switch (item.getName()) {
                    case "Amount" -> amount = Double.valueOf(item.getValue().toString());
                    case "MpesaReceiptNumber" -> receipt = item.getValue().toString();
                    case "TransactionDate" -> txnDate = item.getValue().toString();
                    case "PhoneNumber" -> phone = item.getValue().toString();
                }
            }
        }

        STKCallbackLog log = STKCallbackLog.builder()
                .merchantRequestId(stk.getMerchantRequestID())
                .checkoutRequestId(stk.getCheckoutRequestID())
                .resultCode(stk.getResultCode())
                .resultDesc(stk.getResultDesc())
                .amount(amount)   // will be null for failed txns
                .mpesaReceiptNumber(receipt)
                .transactionDate(txnDate)
                .phoneNumber(phone)
                .build();

        stkPushLogRepository.save(log);

        return Map.of("ResultCode", "0", "ResultDesc", "Callback received successfully");
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
    public ResponseEntity<AcknowledgeResponse> validateTransaction(@RequestBody TransactionResult transactionResult){
        return ResponseEntity.ok(acknowledgeResponse);
    }


//    SIMULATE C2B TRANSACTION
    @PostMapping("/simulate-c2b")
    public ResponseEntity<SimulateC2BResponse> simulateC2BTransaction(@RequestBody SimulateC2BRequest simulateC2BRequest) throws IOException {
        try {
            return ResponseEntity.ok(mpesaService.simulateC2BTransaction(simulateC2BRequest));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
