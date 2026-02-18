package com.jpa.market.dto;

import com.jpa.market.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {

    private String itemName; //상품명

    private int count; //주문수량

    private int orderPrice; //주문금액

    private String imgUrl; //상품이미지 경로

}