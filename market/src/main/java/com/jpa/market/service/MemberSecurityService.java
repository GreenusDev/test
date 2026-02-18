package com.jpa.market.service;

import com.jpa.market.entity.Member;
import com.jpa.market.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor //클래스의 필수 생성자를 자동으로 생성
@Service
public class MemberSecurityService implements UserDetailsService {

    private final MemberRepository memberRepository;

    //UserDetailsService 인터페이스의 메서드를 구현
    //주어진 사용자 이름(username)을 사용하여 사용자 정보를 로드하고 UserDetails 객체로 반환
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        //주어진 사용자 이름으로 회원 정보를 데이터베이스에서 조회
        //주어진 사용자 이름에 해당하는 회원 정보를 반환
        //회원 정보가 없으면 UsernameNotFoundException을 발생
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        //방법 2.
        //role을 자동으로 확인하도록 하기//
        // Spring Security의 User 객체를 생성하여 반환
        //User.builder()를 사용하여 User 객체를 생성하고,
        //username, password, roles를 설정한 뒤 build() 메서드를 호출하여 User 객체를 생성
        //객체를 생성할 때 각 속성을 설정할 수 있어 가독성이 높고, 불필요한 속성을 설정하지 않도록
        return User.builder()
                .username(member.getLoginId())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }
}