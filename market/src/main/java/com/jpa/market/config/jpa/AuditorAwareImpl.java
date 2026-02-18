package com.jpa.market.config.jpa;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

//Auditing 할 때 사용할 사용자 타입은 String이다”
//(보통 loginId, username)
public class AuditorAwareImpl implements AuditorAware<String> {

    //JPA가 호출하는 유일한 메서드
    //✔ “지금 사용자 누구야?”
    @Override
    public Optional<String> getCurrentAuditor() {

        //현재 실행 중인 스레드에서 사용자의 인증 정보를 가져오는 역할
        //SecurityContextHolder: 스프링 시큐리티에서 현재 인증 정보를 보관하는 공유 객체
        //getContext() 메서드: 현재 스레드의 SecurityContext를 반환
        //getAuthentication() 메서드: SecurityContext에서 인증(Authentication) 객체를 가져옴

        //로그인 성공 시 -> Authentication 객체가 -> SecurityContextHolder에 저장됨
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userId = "";

        // 1. 인증 정보가 없거나, 익명 사용자(로그인 전)인 경우
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            // 가입 시점에는 인증 객체가 없으므로 "SYSTEM"이나 "KAKAO_JOIN" 등을 반환
            return Optional.of("SYSTEM");
        }

        // 2. 로그인된 상태라면 실제 아이디(Name) 반환
        return Optional.of(authentication.getName());



    }
}
