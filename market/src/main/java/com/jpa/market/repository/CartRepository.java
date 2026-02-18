package com.jpa.market.repository;

import com.jpa.market.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // Member 엔티티의 'id' 필드(Long)를 참조해서 찾습니다.
    Cart findByMemberId(Long memberId);
}
