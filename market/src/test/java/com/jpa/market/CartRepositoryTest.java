package com.jpa.market;

import com.jpa.market.dto.MemberJoinDto;
import com.jpa.market.entity.Cart;
import com.jpa.market.entity.Member;
import com.jpa.market.repository.CartRepository;
import com.jpa.market.repository.MemberRepository;
import com.jpa.market.service.MemberService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static com.jpa.market.entity.QMember.member;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional  //반복테스트 위해서 롤백 처리 하도록 설정
public class CartRepositoryTest {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberService;

    @Autowired
    PasswordEncoder passwordEncoder;

    //JPA(Entity Manager)를 주입받기 위해 사용
    @PersistenceContext
    EntityManager em;

    public MemberJoinDto createMember() {

        MemberJoinDto dto = new MemberJoinDto();
        dto.setLoginId("green123");
        dto.setPassword("12345678");
        dto.setName("김그린");
        dto.setEmail("green@naver.com");
        dto.setAddress("울산시 남구 삼산동");

        return dto;
    }

    @Test
    @DisplayName("장바구니 회원 엔티티 매핑 조회 테스트")
    public void findCartAndMemberTest() {

        // 1. 회원 정보 생성 (Given)
        MemberJoinDto dto = createMember();

        // 2. 서비스로 회원가입 실행 (Member 엔티티가 DB에 저장됨)
        Long savedMemberId = memberService.joinMember(dto);

        // 3. 저장된 회원 엔티티를 실제로 찾아옴 (Cart와 연결하기 위해)
        Member member = memberRepository.findById(savedMemberId)
                .orElseThrow(EntityNotFoundException::new);

        // 4. 찾은 회원 엔티티를 사용하여 장바구니 생성 및 저장 (When)
        // Cart.createCart는 Member 엔티티를 매개변수로 받아야 함
        Cart cart = Cart.createCart(member);
        cartRepository.save(cart);

        // 5. 영속성 컨텍스트 반영 및 초기화
        em.flush();
        em.clear();

        // 6. 조회 및 검증 (Then)
        System.out.println("로딩 확인");
        Cart savedCart = cartRepository.findById(cart.getId())
                .orElseThrow(EntityNotFoundException::new);

        // 처음에 가입시킨 회원의 ID와 장바구니에 연결된 회원의 ID가 같은지 확인
        assertEquals(savedMemberId, savedCart.getMember().getId());
    }
}
