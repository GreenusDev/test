package com.jpa.market.config.security;

import com.jpa.market.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration      //스프링의 설정파일을 의미
@EnableWebSecurity  //스프링 시큐리티를 활성화하는 어노테이션
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {

        http
                //post는 csrf보호해야 한다고 했었는데
                //CSRF 보호 대상 : 타임리프 폼, 세션 기반 로그인 등
                //우리는 리액트로 처리할꺼라서 csrf 보호 필요없음
                //React + JSON API 환경에서는 불필요
                .csrf(csrf -> csrf.disable())

                // CORS 설정
                //리액트와 포트번호가 다르므로 명시적으로 설정함
                //이 설정 없으면 브라우저에서 요청이 아예 차단됨
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 1. 세션 관리 설정 추가
                //리액트와 세션(쿠키) 방식으로 로그인 정보를 주고받을 계획이라면
                //      IF_REQUIRED(필요시 생성)를 명시하는 것이 안전
                // 세션을 아예 안 쓸 거라면 STATELESS, 세션 쿠키를 쓸 거라면 IF_REQUIRED
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                //권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/main/**", "/api/items/**").permitAll()
                        .requestMatchers("/api/members/login", "/api/members/join").permitAll()
                        // 이미지 파일 경로 (리액트에서 엑박 뜨지 않게 허용)
                        .requestMatchers("/images/**", "/**/*.html", "/").permitAll()
                        // ⭐ 기존 방법 변경하기
                        .requestMatchers("/login/oauth2**").permitAll()
                        //admin으로 시작하는 경로는 해당 계정이 admin일때만 접근가능하도록 설정
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        //나머지는 모두 인증 요구
                        .anyRequest().authenticated() // 나머지는 인증 필요 (보안 강화)
                )

                // React에서 호출할 로그아웃 API 경로
                .logout(logout -> logout
                        .logoutUrl("/api/members/logout")
                        //로그아웃 성공 후 응답을 어떻게 줄지 정의
                        //  req : HttpServletRequest
                        //  res : HttpServletResponse
                        //  auth : 로그아웃 직전의 인증 정보 (보통 null 가능)

                        //“로그아웃이 성공하면
                        //매개변수로 request, response, authentication
                        //이렇게 3개를 받고,
                        //응답으로는 200 상태 코드만 내려준다.”
                        .logoutSuccessHandler((req, res, auth) -> {
                            res.setStatus(200); //HTTP 상태 코드 200만 내려줌
                        })
                )

                // Spring Security 기본 로그인 폼을 완전히 사용하지 않음
                // J리액트에서 로그인 ui를 담당할 예정이므로 끄기
                .formLogin(form -> form.disable())

                // ⭐ 핵심: OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 내가 만든 서비스 등록
                        )
                        .defaultSuccessUrl("/") // 성공 시 이동할 주소
                )


                //인증되지 않은 사용자가 리소스에 접근할때 수행되는 핸들러 등록
                .exceptionHandling((exception)-> exception
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                ;


        //필터 체인을 리턴
        return http.build();
    }

    //비밀번호 암호화 담당
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //스프링 시큐리티의 인증을 실제로 담당하는 핵심 객체입니다.
    // 나중에 컨트롤러에서 직접 로그인을 처리할 때 이 매니저를 호출하게 됩니다.
    // formLogin을 사용할 때는 스프링 시큐리티가 내부적으로 자동으로 설정하고 호출하기 때문에
    // 개발자가 직접 빈으로 등록하거나 관리할 필요가 없었습니다.

    //근데 지금은 리액트와 연동하기 위해 formLogin().disable()을 설정했음.
    //이러면 시큐리티의 자동 로그인 필터가 작동하지 않으므로 직접 빈으로 등록해줘야함
    //시큐리티 인증 그림중에 3번
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 외부 요청 허용 상세 설정)
    //리액트(3000번 포트)에서 스프링(8080번 포트)으로 API를 쏠 때 발생하는 차단을 막아줍니다.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();
        //3000번 포트에서 오는 요청만 신뢰하겠다고 지정
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        //: GET, POST 등 어떤 방식의 요청을 허용할지 정의
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        //어떤 헤더 정보든 다 받겠다고 설정
        config.setAllowedHeaders(List.of("*"));
        //리액트와 스프링이 세션 쿠키를 주고받으려면 이 설정이 반드시 true여야 합니다.
        config.setAllowCredentials(true); // 쿠키/세션 주고받기 허용

        //모든 요청에 대해 CORS 정책 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
