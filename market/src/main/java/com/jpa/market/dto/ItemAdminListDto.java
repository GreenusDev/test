package com.jpa.market.dto;

import com.jpa.market.constant.ItemSellStatus;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ItemAdminListDto {

    private Long id;
    private String itemName;
    private ItemSellStatus itemSellStatus;
    private String createdBy;
    private LocalDateTime regTime;

    //Querydsl이 해당 DTO의 생성자를 알고, 쿼리 결과를 그 생성자에 맞춰 직접 객체로 만들어주도록 지시하는 역할
    @QueryProjection //Querydsl을 사용하여 작성한 쿼리의 결과를 DTO에 매핑
    public ItemAdminListDto(Long id, String itemName, ItemSellStatus itemSellStatus,
                         String createdBy, LocalDateTime regTime) {
        this.id = id;
        this.itemName = itemName;
        this.itemSellStatus = itemSellStatus;
        this.createdBy = createdBy;
        this.regTime = regTime;
    }
}
