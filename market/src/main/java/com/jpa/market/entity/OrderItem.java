package com.jpa.market.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item")
@Getter
//양방향 매핑에서 서로 호출하다 무한루프 발생.
// 연관관계 필드 제외하기
@ToString(exclude = "order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 빌더는 기본적으로 모든 필드를 받는 생성자가 있어야 값을 넣을 수 있음(빌더랑 세트_)
@AllArgsConstructor
@Builder
public class OrderItem  extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; //주문가격

    private int count; //수량


    // --- 연관관계 편의 메서드 ---
    //OrderItem 입장에서 본인의 부모(Order)가 누구인지 알아야 DB 외래 키 컬럼에 데이터가 들어갑니다
    //Setter와는 성격이 다른 '연관관계 설정' 메서드
    public void setOrder(Order order) {
        this.order = order;
    }

    public static OrderItem createOrderItem(Item item, int count) {

        if (count < 1) {
            throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다.");
        }

        OrderItem orderItem = new OrderItem();
        orderItem.item = item;
        orderItem.count = count;
        orderItem.orderPrice = item.getPrice() * count; // 주문 시점 가격 고정

        // [추가] 주문하는 수량만큼 상품의 재고를 감소시킵니다.
        // 이 메서드가 호출되면서 아까 만든 OutOfStockException이 터질 수 있습니다.
        item.removeStock(count);

        // 🔥 연관관계 편의 메서드 대신 여기서만 설정
        return orderItem;
    }

    public void cancelOrderItem() {
        this.getItem().addStock(count); // 해당 상품 재고 복구
    }

    // [추가] 해당 상품 주문 총 가격 (단가 * 수량)
//    public int getTotalPrice() {
//        return orderPrice * count;
//    }
}











