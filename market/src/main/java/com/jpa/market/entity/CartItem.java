package com.jpa.market.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "cart_item")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int count;

    // --- 정적 생성 메서드 ---
    public static CartItem createCartItem(Cart cart, Item item, int count) {

        if (count < 1) {
            throw new IllegalArgumentException("장바구니 수량은 1 이상이어야 합니다.");
        }

        CartItem cartItem = new CartItem();
        cartItem.cart = cart;
        cartItem.item = item;
        cartItem.count = count;
        return cartItem;
    }

    // 이미 담긴 상품의 수량을 늘릴 때 사용할 메서드
    public void addCount(int count) {
        this.count += count;
    }

    //count 수정
    public void updateCount(int count) {
        this.count = count;
    }
}








