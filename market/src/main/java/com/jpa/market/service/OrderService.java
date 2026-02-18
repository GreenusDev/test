package com.jpa.market.service;

import com.jpa.market.dto.OrderDto;
import com.jpa.market.dto.OrderHistDto;
import com.jpa.market.dto.OrderItemDto;
import com.jpa.market.entity.*;
import com.jpa.market.mapper.OrderItemMapper;
import com.jpa.market.mapper.OrderMapper;
import com.jpa.market.repository.ItemImgRepository;
import com.jpa.market.repository.ItemRepository;
import com.jpa.market.repository.MemberRepository;
import com.jpa.market.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemImgRepository itemImgRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public Long order(OrderDto orderDto, String loginId) {

        //EntityNotFoundException
//          1. 서비스(Service): findById로 찾았는데 데이터가 없네?
//                              EntityNotFoundException을 밖으로 던집니다. (비명 발생!)
//          2.컨트롤러(Controller): 서비스가 던진 예외를 따로 try-catch로 잡지 않았기 때문에,
//                              예외는 상위(Spring 프레임워크)로 그대로 전달됩니다.
//          3. 글로벌 핸들러(GlobalExceptionHandler): 스프링이 "누가 이 예외 처리할 사람?" 하고 찾을 때,
//                              핸들러의 @ExceptionHandler(EntityNotFoundException.class)가 손을 번쩍 듭니다.
//          4.응답(Response): 핸들러 메서드가 실행되면서 HttpStatus.NOT_FOUND (404) 상태 코드와 함께
//                              {"message": "해당 정보를 찾을 수 없습니다."}라는 JSON을 리액트에 전송합니다.

        // 1. 주문할 상품 조회
        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("주문하려는 상품이 존재하지 않습니다. (ID: " + orderDto.getItemId() + ")"));

        // 2. 로그인한 회원 정보 조회 (LoginId 기준)
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원을 찾을 수 없습니다. loginId: " + loginId));

        // ... 이후 로직 (OrderItem 생성 및 Order 저장) 동일 ...
        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());

        List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(orderItem);

        // ArrayList 선언과 add 과정을 한 줄로! 이렇게도 가능.
        // 근데 .of는 불변이라 리스트 수정이 불가능함. 나중에 추가하려면 에러남.
        // 지금은 "상품 상세 페이지에서 [바로 구매] 버튼을 눌렀을 때" 동작하는 메서드이므로 of도 괜춘
        //List<OrderItem> orderItemList = List.of(orderItem);

        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();
    }

    public Page<OrderHistDto> getOrderList(String loginId, Pageable pageable) {

        // 1. DB에서 페이징된 주문 엔티티들을 가져옵니다.
        // Page로 받으면 현재 페이지수, 전체페이지수, 전체 데이터 개수, 실제 데이터 리스트를 담게됨
        //사용자의 아이디와 페이징 조건을 이용하여 주문 목록을 조회
        Page<Order> ordersPage = orderRepository.findOrders(loginId, pageable);

        List<OrderHistDto> orderHistDtoList = new ArrayList<>();

        //위 세트 중에서 **"실제 데이터 리스트"**인 List<Order>만 추출
        //만약 사용자님이 한 페이지에 5개씩 보여주기로 설정했다면,
        // getContent()는 현재 페이지에 해당하는 5개의 Order 엔티티가 담긴 리스트가 됩니다.
        for (Order order : ordersPage.getContent()) {

            // [1] Order 엔티티 -> OrderHistDto 변환 (주문 1건에 대한 DTO 생성)부모
            OrderHistDto orderHistDto = orderMapper.entityToDto(order);

            // [2] 해당 주문에 포함된 주문 상품 목록을 조회
            //하나의 주문안에 여러 상품이 있으므로 for 반복
            for (OrderItem orderItem : order.getOrderItems()) {

                // 주문한 상품의 대표 이미지 조회
                //OrderItem에는 이미지 정보 없음. ItemImg는 별도 테이블
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepImgYn(
                        orderItem.getItem().getId(), "Y");

                //이미지가 없으면 url을 공백으로 처리
                String imgUrl = (itemImg != null) ? itemImg.getImgUrl() : "";

                // 주문상품 DTO 생성 (이미지 포함)
                // [3] MapStruct로 상품 DTO 변환 (이미지 경로 포함) -> OrderItem 엔티티 값 복사
                OrderItemDto orderItemDto = orderItemMapper.entityToDto(orderItem, imgUrl);


                // [4] 부모인 주문에 item을 한건씩 추가해줘야함.
                // orderItemDto는 list를 돌면서 하나씩 생성됨. -> 리스트를 한번에 넣을 수 없음.
                //그래서 '하나 추가하는 메서드'가 필요함
                orderHistDto.addOrderItemDto(orderItemDto);
            }

            //주문 단위 DTO 리스트에 추가 -> 하나의 주문 완성
            orderHistDtoList.add(orderHistDto);
        }

        // 3. 최종적으로 원래의 페이징 정보(totalElements 등)를 유지하며 리턴
        //content : DTO 리스트
        //pageable : 요청받은 페이지 정보
        //totalElements : 엔티티 Page에서 가져온 전체 개수
        return new PageImpl<>(orderHistDtoList, pageable, ordersPage.getTotalElements());
    }

    public void cancelOrder(Long orderId, String loginId) {
        // 1. 취소할 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("취소하려는 주문이 존재하지 않습니다. (주문 ID: " + orderId + ")"));

        // 2. 보안 체크: 로그인한 사용자와 주문자가 같은지 확인
        // Member 엔티티의 식별자가 loginId인지 이메일인지에 따라 .getLoginId() 또는 .getEmail() 사용
        if (!order.getMember().getLoginId().equals(loginId)) {

            //AccessDeniedException : 수정/삭제 권한이 없을 때 가장 표준적으로 사용하는 예외 (403)
            // 401 인증 실패, 403 인가/권한 실패
            // /admin 같은건 애초에 시큐리티에서 확인하니까 서비스까지 올 수가 없음.
            // 이건 인증은 성공했는데 내 것이 아니라서 권한이 없는거니까 예외 처리하기
            throw new AccessDeniedException("주문 취소 권한이 없습니다.");
        }

        // 3. 주문 취소 실행 (내부에서 상태 변경 및 재고 복구 로직 호출)
        order.cancelOrder();
    }

    /**
     * 장바구니에서 여러 개의 상품을 선택하여 주문할 때 사용하는 다중 주문 메서드
     */
    public Long orderMultipleItems(List<OrderDto> orderDtoList, String loginId) {

        // 1. 주문자 정보 조회
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원을 찾을 수 없습니다. loginId: " + loginId));

        List<OrderItem> orderItemList = new ArrayList<>();

        // 2. 주문할 상품 리스트를 돌면서 OrderItem 엔티티 생성
        for (OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. ID: " + orderDto.getItemId()));

            // OrderItem 생성 (이 안에서 재고 감소 로직이 실행됨)
            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
            orderItemList.add(orderItem);
        }

        // 3. 하나의 주문(Order) 엔티티 생성 (여러 개의 주문상품 리스트를 포함)
        Order order = Order.createOrder(member, orderItemList);

        // 4. DB 저장
        orderRepository.save(order);

        return order.getId();
    }
}









