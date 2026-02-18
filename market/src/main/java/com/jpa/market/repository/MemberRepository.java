package com.jpa.market.repository;

import com.jpa.market.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    //회원가입할때 중복되노 회원이 있는지 검사하기 위해 로그인id로 회원을 검사하도록
    //쿼리 메서드를 작성함
    //존재여부 확인(true / false 반환)

    //id 중복검사
    boolean existsByLoginId(String loginId);

    //중복된 메일주소 있는지 확인(기존 가입자)
    boolean existsByEmail(String email);

    //로그인 처리를 위해 id에 해당하는 사람 찾아오기
    //비번은 시큐리티가 처리
    Optional<Member> findByLoginId(String loginId);
}
