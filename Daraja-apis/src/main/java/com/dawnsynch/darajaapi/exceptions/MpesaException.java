package com.dawnsynch.darajaapi.exceptions;

public class MpesaException extends RuntimeException {

    public MpesaException(String message) {
        super(message);
    }

    public MpesaException(String message, Throwable cause) {
        super(message, cause);
    }

    public MpesaException() {
    }
}

