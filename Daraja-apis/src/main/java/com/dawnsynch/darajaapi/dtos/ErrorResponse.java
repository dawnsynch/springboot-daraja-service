package com.dawnsynch.darajaapi.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ErrorResponse {

    private String error;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;

    public ErrorResponse(String error, String message, int status, String path) {
        this.error = error;
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}
