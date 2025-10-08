package com.dawnsynch.darajaapi.exceptions;


import com.dawnsynch.darajaapi.dtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception ex, HttpStatus status, String errorTitle, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                errorTitle,
                ex.getMessage(),
                status.value(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(MpesaAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(MpesaAuthenticationException ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "M-Pesa Authentication Error", request);
    }

    @ExceptionHandler(MpesaNetworkException.class)
    public ResponseEntity<ErrorResponse> handleNetwork(MpesaNetworkException ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.SERVICE_UNAVAILABLE, "M-Pesa Network Error", request);
    }

    @ExceptionHandler(MpesaRequestException.class)
    public ResponseEntity<ErrorResponse> handleRequest(MpesaRequestException ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Invalid M-Pesa Request", request);
    }

    @ExceptionHandler(MpesaResponseException.class)
    public ResponseEntity<ErrorResponse> handleResponse(MpesaResponseException ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_GATEWAY, "M-Pesa Response Error", request);
    }

    @ExceptionHandler(MpesaProcessingException.class)
    public ResponseEntity<ErrorResponse> handleProcessing(MpesaProcessingException ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "M-Pesa Processing Error", request);
    }

    // Generic fallback for any other unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected Error", request);
    }
}
