package com.jpa.market.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CartOrderDto {

    // 역할: "사용자가 화면에서 체크한 장바구니 번호들의 집합"
    private List<Long> cartItemIds;

}
