package com.jpa.market.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "cart")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {
    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    //Cart엔티티를 생성하는 메서드
    public static Cart createCart(Member member) {
        Cart cart = new Cart();
        cart.member = member; // 엔티티 내부에서는 필드에 직접 접근 가능
        return cart;
    }
}
