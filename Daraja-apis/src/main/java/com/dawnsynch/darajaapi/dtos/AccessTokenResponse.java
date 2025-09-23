package com.dawnsynch.darajaapi.dtos;

import lombok.Data;

@Data
public class AccessTokenResponse {

    private String access_token;
    private String expires_in;
}
