package com.jpa.market.service;

import com.jpa.market.dto.KakaoTokenDto;
import com.jpa.market.entity.Member;
import com.jpa.market.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoService{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${kakao.default.password:kakao}")
    private String kakaoDefaultPassword;

    public KakaoTokenDto getKakaoAccessToken(String code) {
        // 1. 통신에 필요한 주소 설정
        String reqURL = "https://kauth.kakao.com/oauth/token";

        // 2. RestTemplate 선언
        //RestTemplate : Spring에서 제공하는 객체로
        //브라우저 없이 Http요청을 처리할 수 있음
        //브라우저가 서버에 HTTP요청을 전송하는 것 처럼 프로그램에서 HTTP요청을 처리할 수 있음
        RestTemplate rt = new RestTemplate();

        // 3. HttpHeader 오브젝트 생성 (카카오 요구 사항: 공식 문서 기반)
        //HttpHeaders 생성(HTTP요청 헤더)
        //요청의 내용이 URL 인코딩된 데이터임을 의미함
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 4. HttpBody 오브젝트 생성 (전달할 데이터들)
        //HttpBody생성(4개의 필수 매개변수를 설정함)
        //MultiValueMap : 맵의 확장형
        //기존의 Map과의 차이는
        // Map : 하나의 키와 하나의 값으로 이루어짐.
        //		 동일한 키가 입력되면 마지막 값이 최종 저장됨
        //MultiValueMap : 하나의 키와 하나 이상의 값으로 이루어짐
        //				  값을 리스트형태로 저장(값을 모두 저장함)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "d87e69bdc474e83377581de6926ecee1"); // 발급받은 키 입력
        params.add("client_secret", "ZUt4MntR72dXyc3jwbTfg7ileORZufPO"); // ⭐ 추가
        params.add("redirect_uri", "http://localhost:8001/auth/members/kakao"); // 등록한 URI
        params.add("code", code); // 방금 받은 인가 코드

        // 5. Header와 Body를 하나로 합침
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        // 6. 실제 요청 보내기 (POST 방식)
        //HTTP 요청을 보내고 그에 대한 응답을 받음
        ResponseEntity<String> response = rt.exchange(
                reqURL,             //액세스 토큰 요청 주소
                HttpMethod.POST,    //요청방식
                kakaoTokenRequest,  //요청 헤더와 바디
                String.class        //응답받을 타입
        );

        //👉 JSON ↔ Java 객체 변환기
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            //response.getBody() :  카카오가 준 응답 (문자열)
            //KakaoTokenDto.class: “이 JSON을 이 클래스 형태로 바꿔줘”
            return objectMapper.readValue(response.getBody(), KakaoTokenDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카카오 토큰 파싱 실패", e);
        }
    }

    //위에꺼 복사해서 고치기
    public String getKakaoUserInfo(KakaoTokenDto tokenDto) {
        // 1. 카카오 사용자 정보 요청을 위한 API 주소
        String reqURL = "https://kapi.kakao.com/v2/user/me";

        // 2. HTTP 요청을 보내기 위한 RestTemplate 객체 생성
        RestTemplate rt = new RestTemplate();

        // 3. HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        // 발급받은 액세스 토큰을 Authorization 헤더에 담음 (Bearer 뒤 공백 필수!)
        headers.add("Authorization", "Bearer " + tokenDto.getAccess_token());
        // 요청 본문의 데이터 타입을 설정 (카카오 가이드 준수)
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 4. 헤더 정보를 담은 HttpEntity 객체 생성 (전송용 패키지 구성)
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        // 3. 사용자 정보 요청
        // HTTP 요청을 POST(GET) 방식으로 실행 -> 응답(JSON 문자열)을 ResponseEntity로 받음
        ResponseEntity<String> response = rt.exchange(
                reqURL,
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        // 카카오 인증 서버가 반환한 사용자 정보
        return response.getBody();
    }

}