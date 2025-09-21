package com.dawnsynch.darajaapitutorial.dtos;

import lombok.Data;

@Data
public class AccessTokenResponse {

    private String access_token;
    private String expires_in;
}
