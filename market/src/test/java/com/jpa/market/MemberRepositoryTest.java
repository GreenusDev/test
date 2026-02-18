package com.jpa.market;

import com.jpa.market.dto.MemberJoinDto;
import com.jpa.market.entity.Member;
import com.jpa.market.repository.MemberRepository;
import com.jpa.market.service.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
//DB 확인위해서 롤백 안되도록.
@Transactional
//@Rollback(false) // 중요: 테스트가 끝나도 롤백하지 말고 DB에 남겨라!
public class MemberRepositoryTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    //추가
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    EntityManager em; // em.flush()를 위해 주입

    //회원 정보를 입력한 member엔티티를 만드는 메서드를 작성함
    // 회원 정보를 입력한 member 엔티티를 만드는 메서드
    public MemberJoinDto createMember() {

        MemberJoinDto dto = new MemberJoinDto();
        dto.setLoginId("green1");
        dto.setPassword("12345678");
        dto.setName("김그린");
        dto.setEmail("green@naver.com");
        dto.setAddress("울산시 남구 삼산동");

        return dto;

    }

    @Test
    @DisplayName("회원가입 테스트")
    public void saveMemberTest() {
        // given
        MemberJoinDto dto = createMember();

        // when
        Long savedId = memberService.joinMember(dto);

        //.orElseThrow() : 데이터가 없으면 예외를 던지고, 있으면 객체를 꺼내라
        //값이 있으면: Optional 박스 안에 있는 Member 객체를 꺼내서 반환합니다.
        //값이 없으면(null): NoSuchElementException이라는 예외를 즉시 발생시킵니다.
        Member savedMember = memberRepository.findById(savedId).orElseThrow();

        assertThat(savedMember.getLoginId()).isEqualTo(dto.getLoginId());
        assertThat(savedMember.getName()).isEqualTo(dto.getName());
        assertThat(savedMember.getEmail()).isEqualTo(dto.getEmail());
        assertThat(savedMember.getAddress()).isEqualTo(dto.getAddress());
        assertThat(savedMember.getRole()).isNotNull();
    }

    @Test
    @DisplayName("중복 가입 테스트")
    public void saveMemberTest2() {
        // given
        MemberJoinDto dto1 = createMember();
        MemberJoinDto dto2 = createMember();

        memberService.joinMember(dto1);

        try {
            memberService.joinMember(dto2);
        } catch (IllegalStateException e) {
            assertThat("이미 사용중인 아이디입니다.").isEqualTo(e.getMessage());
        }

    }

    @Test
    @DisplayName("Auditing 테스트")
    @WithMockUser(username = "green", roles = "USER")
    public void auditingTest(){
        // Member 생성 시 필수값인 loginId를 채워줌
        MemberJoinDto dto = new MemberJoinDto();
        dto.setLoginId("green123");
        dto.setPassword("12345678");
        dto.setName("김그린");
        dto.setEmail("green@naver.com");
        dto.setAddress("울산시 남구 삼산동");

        // 이미 만들어둔 static 메서드 활용 (권장)
        Member newMember = Member.createMember(dto, passwordEncoder);
        memberRepository.save(newMember);

        em.flush();
        em.clear();

        Member member = memberRepository.findById(newMember.getId())
                .orElseThrow(EntityNotFoundException::new);

        // 출력 확인
        System.out.println("register time : " + member.getRegTime());
        System.out.println("update time : " + member.getUpdateTime());
        System.out.println("create member : " + member.getCreatedBy());
        System.out.println("modify member : " + member.getModifiedBy());
    }


}
