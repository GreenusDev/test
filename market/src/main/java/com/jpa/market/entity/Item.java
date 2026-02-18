package com.jpa.market.entity;

import com.jpa.market.config.exception.OutOfStockException;
import com.jpa.market.constant.ItemSellStatus;
import com.jpa.market.dto.ItemFormDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

// 어노테이션 설명은 밑에!!!
@Entity
@Table(name = "item")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 빌더는 기본적으로 모든 필드를 받는 생성자가 있어야 값을 넣을 수 있음(빌더랑 세트_)
@AllArgsConstructor
@Builder
public class Item extends BaseEntity {

    //2.어노테이션 추가
    //전부 persistence로 들어가므로 어노테이션 뒤를 *로 변경할것

    @Id    //테이블의 기본키로 지정
    @Column(name = "item_id")    //테이블에 매핑될 컬럼의 이름 설정(컬럼명이랑 다를떄만 설정)
    @GeneratedValue(strategy = GenerationType.IDENTITY) //기본키 생성 전략을 AUTO로 지정함
    private Long id; //--상품코드

    @Column(nullable = false, length = 50) //nullable : not null,
    private String itemName; //--상품명

    @Column(nullable = false)
    private int price; //--가격

    @Column(nullable = false)
    private int stockNumber; //--재고수량

    @Lob
    @Column(nullable = false)
    private String itemDetail; //--상품 상세 설명

    @Enumerated(EnumType.STRING)   //enum 타입 매핑
    private ItemSellStatus itemSellStatus; //--상품 판매 상태

    // --- 여기를 추가해 주세요! ---
    // 양방향 매핑: ItemImg 엔티티에 있는 'item' 필드에 의해 매핑됨
    // cascade = CascadeType.ALL: 상품 저장할 때 이미지도 같이 저장됨
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc") // 이 한 줄이 있으면 item.getItemImgs() 할 때 항상 ID 순으로 정렬됩니다!
    private List<ItemImg> itemImgs = new ArrayList<>();


    // 아직 메모리에 Item 객체가 존재하지 않는 상태에서
    // "이런 재료(DTO)를 줄 테니 상품 하나 만들어줘"라고 요청하는 것이므로,
    // 클래스 수준에서 호출하는 static 메서드(정적 팩토리 메서드)가 적합합니다.
    public static Item createItem(ItemFormDto itemFormDto) {
        Item item = new Item();
        item.itemName = itemFormDto.getItemName();
        item.price = itemFormDto.getPrice();
        item.stockNumber = itemFormDto.getStockNumber();
        item.itemDetail = itemFormDto.getItemDetail();
        item.itemSellStatus = itemFormDto.getItemSellStatus();
        // regTime, updateTime은 @PrePersist가 자동으로 처리하므로 비워둠
        return item;
    }

    // 2. 수정을 위한 인스턴스 메서드 (static 아님)
    //이미 존재하는 특정 상품의 **'상태를 변경'**하는 단계
    //해당 객체 스스로가 자기 데이터를 수정할 수 있도록 인스턴스 메서드로 생성
    public void updateItem(ItemFormDto itemFormDto) {
        this.itemName = itemFormDto.getItemName();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
        // 여기서 this는 JPA가 DB에서 조회해온 바로 '그' 상품 객체를 가리킵니다.
    }

    // 1. 재고 감소 로직
    public void removeStock(int stockNumber) {
        int restStock = this.stockNumber - stockNumber;

        // 재고가 부족하면 우리가 만든 예외를 던짐
        if (restStock < 0) {
            throw new OutOfStockException("상품의 재고가 부족합니다. (현재 재고 수량: " + this.stockNumber + ")");
        }

        this.stockNumber = restStock;

        // ⭐ 재고가 0이 되면 상태를 자동으로 SOLD_OUT으로 변경
        if (this.stockNumber == 0) {
            this.itemSellStatus = ItemSellStatus.SOLD_OUT;
        }
    }

    // 2. 재고 증가 로직 (주문 취소 시 필요)
    public void addStock(int stockNumber) {
        this.stockNumber += stockNumber;

        // 재고가 0보다 커지면 다시 판매 중 상태로 변경
        if (this.stockNumber > 0) {
            this.itemSellStatus = ItemSellStatus.SELL;
        }
    }

}









