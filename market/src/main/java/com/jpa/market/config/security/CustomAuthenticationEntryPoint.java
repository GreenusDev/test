package com.jpa.market.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

// Spring Security에서 사용되는 인증 진입점(Authentication Entry Point)를
//커스터마이즈하기 위한 클래스입니다.

//로그인하지 않은 사용자가 보호된 API를 호출했을 때,
//서버가 “401 Unauthorized”로 응답하도록 만드는 역할

//인증 진입점은 인증되지 않은 요청이 보호된 리소스에 접근하려고 할 때 호출되는 지점
//인증되지 않은 사용자가 접근을 시도할 경우, 인증 진입점은 적절한 응답을 생성하고 클라이언트에게 전달

// CustomAuthenticationEntryPoint클래스는 컨트롤러에서도, 서비스에서도, 필터에서도 직접 부르지 않음
//👉 Spring Security가 자동으로 호출

//호출되는 시점 인증(Authentication)이 안 된 상태에서
//인증이 필요한 리소스에 접근했을 때
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // commence() 메서드는 인증이 필요한 리소스에 접근할 때 호출되며,
    //인증 예외(AuthenticationException)가 발생한 경우 실행함
    //매개변수 (요청, 응답, 인증 실패 이유)
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        //commence() 메서드가 호출되면 response.sendError()를 사용하여 HTTP 응답을 생성하고,
        //상태 코드를 HttpServletResponse.SC_UNAUTHORIZED로 설정
        //클라이언트에게 "Unauthorized"라는 상태 코드를 설정하고 알려줌.
        //redirect 아님!!! 그냥 상태 코드만 보냄

        //SC_UNAUTHORIZED : 권한이 없음을 나타내는 상태코드 (401)
        //response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // ⭐ 상태 코드만
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write(
                "{ \" message \" : \"로그인이 필요한 서비스입니다. \" } "
        );

    }

}