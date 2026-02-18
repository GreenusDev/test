package com.jpa.market.controller;

import com.jpa.market.dto.LoginRequestDto;
import com.jpa.market.dto.MemberJoinDto;
import com.jpa.market.entity.Member;
import com.jpa.market.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    //"인증(Authentication)을 총괄하는 관리자(Manager)" 역할
    //SecurityConfig에 등록함
    private final AuthenticationManager authenticationManager;

    @PostMapping("/join")
    public ResponseEntity<Long> join(@RequestBody @Valid MemberJoinDto dto) {
        // 화면이 있을땐 예외화면으로 전송했는데
        //우린 템플릿엔진을 사용하지 않기때문에 상태만 전달해야함
        //예외 처리 클래스 생성해서 사용해야함

        // 유효성 검사 실패 시 ExceptionHandler가 실행됨
        // 중복 회원 발생 시 ExceptionHandler가 실행됨
        Long memberId = memberService.joinMember(dto);

        return ResponseEntity.ok(memberId);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request, HttpServletRequest httpRequest) {
        try {
            // 1. 로그인 시도용 토큰 생성 (아직 인증 X)
            //      "여기 이 정보(Id/Pw) 담긴 종이(Token)가 있습니다. 확인해 주세요."
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.getLoginId(), request.getPassword());

            // 2. 인증 수행 (여기서 UserDetailsService + PasswordEncoder 작동)
            //      관리자(Manager)에게 종이를 넘깁니다.
            //      관리자는 내부적으로 MemberSecurityService(DB조회) + BCrypt(비번대조)를 실행합니다.
            Authentication authentication = authenticationManager.authenticate(authToken);

            // 3. 3. 인증 성공 시 SecurityContext에 저장 (서버가 이 유저를 기억함)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 4. 세션 생성 (Spring Security가 인증 정보를 세션에 저장)
            HttpSession session = httpRequest.getSession(true);
            //SecurityConfig에서 formLogin().disable()했기 때문에
            //스프링 시큐리티의 기본 로그인 로직이 동작을 안하므로
            //세션을 생성한 후에 시큐리티에서 약속된 Key값으로 인증 정보를 세션에 넣어줘야
            //로그인 후에 작업할 때 '이 세션은 로그인된 유저구나!'라고 써먹을 수 있음.
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // 5. 리액트를 위해 사용자 정보 응답
            // authentication.getName()은 우리가 설정한 LoginId를 반환합니다.
            return ResponseEntity.ok().body(java.util.Map.of(
                    "message", "로그인 성공",
                    "loginId", authentication.getName(), //UserDetails.getUsername()의 반환값
                    "role",  authentication.getAuthorities()    //설명있음
                            .stream()
                            .map(a -> a.getAuthority())
                            .toList()

            ));

        } catch (Exception e) {
            // 인증 실패 시 (비밀번호 틀림 등)
            return ResponseEntity.status(401).body(java.util.Map.of("message", "아이디 또는 비밀번호가 틀렸습니다."));
        }
    }
}

