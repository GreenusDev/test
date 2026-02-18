package com.jpa.market.dto;

import com.jpa.market.constant.OrderStatus;
import com.jpa.market.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class OrderHistDto {

    private Long orderId;      // 주문 아이디
    private String orderDate;  // 주문 날짜
    private OrderStatus orderStatus; // 주문 상태

    // ⭐ 핵심: 주문 상품 리스트를 담고 있음 (작은 상자들)
    private List<OrderItemDto> orderItemDtoList = new ArrayList<>();

    //부모 DTO(OrderHistDto)를 만들고 난 뒤에,
    // 자식 DTO(OrderItemDto)를 하나씩 “조립하듯” 넣기 위해 필요하다. (서비스에서 사용)
    public void addOrderItemDto(OrderItemDto orderItemDto) {
        orderItemDtoList.add(orderItemDto);
    }

    //MapStruct를 이용하므로 아래의 생성자도 필요없어짐.

    // 생성자: Order 엔티티를 받아서 DTO로 변환
//    public OrderHistDto(Order order) {
//        this.orderId = order.getId();
//        // 리액트에서 보여주기 좋게 날짜 포맷팅 (예: 2026-01-21 12:00)
//        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
//        this.orderStatus = order.getOrderStatus();
//    }
}
