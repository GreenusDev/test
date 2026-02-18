package com.jpa.market.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MainItemDto {

    private Long id;

    private String itemName;

    private String itemDetail;

    private String imgUrl;

    private Integer price;


    //Item객체로 값을 받은 후 Dto클래스로 변환하는 과정없이 바로 MainItemDto객체로 추출함
    // 조인 결과를 바로 DTO로 받기 위해 사용
    @QueryProjection //Querydsl을 사용하여 작성한 쿼리의 결과를 DTO에 매핑
    public MainItemDto(Long id, String itemName, String itemDetail,
                       String imgUrl, Integer price) {
        this.id = id;
        this.itemName = itemName;
        this.itemDetail = itemDetail;
        this.imgUrl = imgUrl;
        this.price = price;
    }
}