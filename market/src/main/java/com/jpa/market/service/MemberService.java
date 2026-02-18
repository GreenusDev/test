package com.jpa.market.service;

import com.jpa.market.dto.MemberJoinDto;
import com.jpa.market.entity.Member;
import com.jpa.market.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
//비즈니스 로직을 담당하는 서비스 계층 클래스에 트랜잭셔널 어노테이션 선언
//로직을 처리하다가 에러가 발생하면
//변경된 데이터를 로직 수행 이전상태로 콜백함
@Transactional
@RequiredArgsConstructor
public class MemberService {
    //final이므로 생성자를 생성하고 빈으로 주입함
    private final MemberRepository memberRepository;

    //패스워드 암호화를 위해 주입
    private final PasswordEncoder passwordEncoder;

    public Long joinMember(MemberJoinDto dto){
        validateDuplicateMember(dto);
        Member member = Member.createMember(dto, passwordEncoder);
        memberRepository.save(member);

        return member.getId();
    }

    //이미 가입된 회원이면 예외 발생함
    private void validateDuplicateMember(MemberJoinDto dto){

        if (memberRepository.existsByLoginId(dto.getLoginId())) {
            throw new IllegalStateException("이미 사용중인 아이디입니다.");
        }

        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("이미 사용중인 이메일입니다.");
        }
    }


}
