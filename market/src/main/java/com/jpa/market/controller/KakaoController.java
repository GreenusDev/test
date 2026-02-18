package com.jpa.market.controller;

import com.jpa.market.dto.KakaoTokenDto;
import com.jpa.market.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller // 데이터를 바로 반환하거나 리다이렉트 처리를 위해
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;

    @GetMapping("/auth/members/kakao")
    public @ResponseBody String kakaoLogin(@RequestParam("code") String code) {
        // 1. 토큰 객체 받기
        KakaoTokenDto tokenDto = kakaoService.getKakaoAccessToken(code);

        // 2. 토큰을 넘겨서 사용자 정보 받기
        String userInfo = kakaoService.getKakaoUserInfo(tokenDto);

        return "받아온 사용자 정보: " + userInfo;
    }
}
