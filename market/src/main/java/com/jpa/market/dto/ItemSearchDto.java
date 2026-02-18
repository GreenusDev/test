package com.jpa.market.dto;

import com.jpa.market.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemSearchDto {

    //현재 시간과 상품 등록일을 비교해서 조회
    //all, 1d, 1w, 1m, 6m
    private String searchDateType;

    //상품의 판매 상태를 기준으로 조회
    private ItemSellStatus searchSellStatus;

    //상품을 조회할때의 유형(상품명 또는 상품을 등록한 사람의 아이디)
    private String searchBy;

    //조회할 검색어를 저장하는 변수
    private String searchQuery = "";

}
