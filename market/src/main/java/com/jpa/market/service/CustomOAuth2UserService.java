package com.jpa.market.service;

import com.jpa.market.constant.OAuthType;
import com.jpa.market.entity.Member;
import com.jpa.market.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. OAuth2 서버에 사용자 정보 요청
        //    (토큰 포함 요청은 부모 클래스가 처리)
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. OAuth2 서버에서 내려준 사용자 정보를 Map 형태로 받음
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 3. 어느 소셜 로그인인지 확인 (kakao 또는 naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 4. 소셜별 사용자 정보 담을 변수 선언
        String providerId = "";      // 소셜에서 발급한 고유 ID
        String nickname = "";        // 닉네임
        String email = "";           // 이메일
        OAuthType oauthType = null;  // 소셜 타입 (KAKAO, NAVER 등)
        String nameAttributeKey = ""; // OAuth2User 식별 키

        if ("kakao".equals(registrationId)) {
            // 카카오 고유 사용자 ID
            providerId = attributes.get("id").toString();

            // 카카오 계정 정보
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            email = (String) kakaoAccount.get("email");
            nickname = (String) profile.get("nickname");
            oauthType = OAuthType.KAKAO;
            nameAttributeKey = "id";


        /* ===================== NAVER ===================== */
        }  else if ("naver".equals(registrationId)) {
            // 네이버는 모든 정보가 "response"라는 키 안에 json형태로 들어있음
            // 네이버 응답에서 실제 사용자 정보가 담긴 response 객체 추출
            Map<String, Object> response =
                    (Map<String, Object>) attributes.get("response");

            // 네이버에서 발급한 고유 사용자 ID
            // (네이버 내부에서만 유일한 값)
            providerId = (String) response.get("id");
            nickname = (String) response.get("name");   //네이버는 이름 제공함
            email = (String) response.get("email");

            oauthType = OAuthType.NAVER;

            // Spring Security가 OAuth2User를 식별할 때 사용할 키
            // 네이버의 실제 사용자 ID는 response 안의 id이므로 "id" 사용
            nameAttributeKey = "response";

        } else {
            // 지원하지 않는 소셜 로그인 요청 시
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth Provider");
        }

        // 4. 소셜 타입 + 소셜 ID 조합으로 로그인 ID 생성 (KAKAO_123456789 형태)
        String loginId = oauthType.name() + "_" + providerId;

        //람다식 안에서 사용하는 지역 변수는
        //final 이거나 값을 한 번만 대입하고,
        //그 이후로 절대 변경되지 않는 변수여야 한다.
        //위에서는 값을 선언한 후 if문에서 값이 변경되므로 람다식에서 사용할 수 없으므로
        //변수에 다시 저장해서 값을 대입함
        final String finalNickname = nickname;
        final String finalEmail = email;
        final OAuthType finalOauthType = oauthType;

        //DB에 회원이 있으면 조회, 없으면 신규 회원 생성
        Member member = memberRepository.findByLoginId(loginId)
                .orElseGet(() -> {
                    // OAuth 회원용 임시 비밀번호 생성
                    String encodedPwd = passwordEncoder.encode("OAUTH_" + UUID.randomUUID());
                    // 기존에 엔티티에 만든 생성 메서드 호출
                    Member newMember = Member.createOAuthMember(loginId, finalNickname, finalEmail, encodedPwd, finalOauthType);
                    return memberRepository.save(newMember);
                });

        // 5. 시큐리티가인증 객체로 사용할 OAuth2User(유저 객체) 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())),
                attributes,
                nameAttributeKey// 카카오 응답의 식별자인 "id"를 키로 설정
        );
    }
}
