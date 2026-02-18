package com.jpa.market.dto;

import lombok.Data;

@Data // Lombok 사용 시
public class KakaoTokenDto {
    private String access_token;
    private String token_type;
    private String refresh_token;
    private int expires_in;
    private String scope;
    private int refresh_token_expires_in;
}