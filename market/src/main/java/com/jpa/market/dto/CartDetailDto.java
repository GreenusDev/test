package com.jpa.market.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDetailDto {

    private Long cartItemId; // 장바구니 상품 아이디

    private String itemName; // 상품명

    private int price; // 상품 금액

    private int count; // 수량

    private String imgUrl; // 상품 이미지 경로

//    // [중요] JPQL에서 'new'로 조회할 때 이 생성자 순서와 파라미터가 쿼리와 일치해야 합니다.
    public CartDetailDto(Long cartItemId, String itemName, int price, int count, String imgUrl) {
        this.cartItemId = cartItemId;
        this.itemName = itemName;
        this.price = price;
        this.count = count;
        this.imgUrl = imgUrl;
    }
}
