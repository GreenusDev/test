package com.jpa.market.entity;

import com.jpa.market.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
//양방향 매핑에서 서로 호출하다 무한루프 발생.
// 연관관계 필드 제외하기
@ToString(exclude = "orderItems")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order  extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime orderDate; //주문일

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; //주문상태

    // --- 양방향 관계 설정 ---
    // orphanRemoval = true: OrderItem 리스트에서 제거된 객체는 DB에서도 삭제
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();


    //Java 객체 세상에서는 양쪽을 다 연결해줘야 합니다.
    // order에 아이템을 넣으면서 동시에 orderItem도 본인의 부모가 누구인지 알게 세팅해주는 역할
    //내부적으로 두 가지 일을 동시에 처리하는 **'연관관계 편의 메서드'**
    public void addOrderItem(OrderItem orderItem) {
        //부모를 저장할 때 부모의 리스트 안에 들어있는 객체들을 보고
        // "아, 얘네도 같이 저장(전이)해야겠구나"라고 판단합니다.
        // 리스트에 넣지 않으면 전이가 일어나지 않습니다.
        this.orderItems.add(orderItem); // 1. Order가 자식을 알게 함 (Cascade용)

        //개발자가 실수로 자식에게 부모를 알려주지 않는 일을 방지
        orderItem.setOrder(this);       // 2. OrderItem이 부모를 알게 함 (DB 외래키 저장용)
    }

    // --- 정적 생성 메서드 ---
    // 주문할 상품의 리스트를 받아서 한번에 처리하도록 하기위해 추가하기
    public static Order createOrder(Member member, List<OrderItem> orderItemList) {
        Order order = new Order();
        order.member = member;

        // 전달받은 여러 개의 주문 상품(OrderItem)을 하나씩 Order에 담습니다.
        // 이 과정에서 addOrderItem()이 호출되어 양방향 연결이 완성됩니다.
        for (OrderItem orderItem : orderItemList) {
            order.addOrderItem(orderItem);
        }

        order.orderStatus = OrderStatus.ORDER;
        order.orderDate = LocalDateTime.now();
        return order;
    }

    // [추가] 전체 주문 금액을 계산하는 메서드 (리액트에서 총 결제 금액 보여줄 때 유용함)
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            //totalPrice += orderItem.getTotalPrice(); // OrderItem의 계산 메서드 호출
            totalPrice += orderItem.getOrderPrice(); // OrderItem의 계산 메서드 호출

        }
        return totalPrice;
    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL; // 주문 상태 변경

        for (OrderItem orderItem : orderItems) {
            orderItem.cancelOrderItem(); // 각 주문 상품들 취소 처리
        }
    }


}








