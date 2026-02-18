package com.jpa.market.repository;

import com.jpa.market.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    //:loginId -> 쿼리에서 메서드의 파라미터값으로 변경됨
    //@Param -> 변수와 메서드의 파라미터를 연결하기 위해 사용

    //Pageable을 매개변수로 전달하면 현재 페이지 번호 (page), 한 페이지 크기 (size), 정렬 정보 (Sort)
    // 1. 특정 사용자의 주문 목록을 최신순으로 조회
    @Query("select o from Order o " +
            "where o.member.loginId = :loginId " + // email 대신 loginId로!
            "order by o.orderDate desc")
    Page<Order> findOrders(@Param("loginId") String loginId, Pageable pageable);

    // 2. 특정 사용자의 전체 주문 개수 조회 -> Pageable이 알아서 계산해주므로 필요없음
//    @Query("select count(o) from Order o " +
//            "where o.member.loginId = :loginId")
//    Long countOrder(@Param("loginId") String loginId);
}
