package com.jpa.market.controller;

import com.jpa.market.dto.OrderDto;
import com.jpa.market.dto.OrderHistDto;
import com.jpa.market.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<?> order(@RequestBody @Valid OrderDto orderDto,
                                   Principal principal) {

            // 서비스 호출 (LoginId 추출 및 주문 실행)
            // Principal.getName()은 사용자가 로그인할 때 썼던 LoginId를 반환함
            //(예외 발생 시 GlobalExceptionHandler가 처리)
            String loginId = principal.getName();
            Long orderId = orderService.order(orderDto, loginId);

            // 3. 성공 시 생성된 주문번호(ID) 반환
            return ResponseEntity.ok(orderId);
    }

    // 매핑에 "" 이거 안넣어두면 /api/orders로 매핑이 안됨
    // /{page}만 들어가게되어서 무조건 page있는걸로 받아옴
    @GetMapping(value = {"", "/{page}"})
    public ResponseEntity orderHist(@PathVariable("page") Optional<Integer> page,
                                    Principal principal) {

        //로그인한 사용자의 정보 가져옴.
        //SecurityConfig에서 막혀있으므로 null일리가 없으므로 검사도 하지 않음
        String loginId = principal.getName();

        // 2. 페이징 설정: 요청받은 페이지 번호가 없으면 0, 한 페이지에 4개씩 최신순 조회
        // Pageable → 인터페이스 (약속) / PageRequest → 구현체 (실제 객체)
        Pageable pageable = PageRequest.of(page.orElse(0), 4);

        // 3. 서비스 호출: loginId와 페이징 정보를 전달
        Page<OrderHistDto> orderHistDtoList = orderService.getOrderList(loginId, pageable);

        // 4. 결과 반환: Page 객체를 그대로 넘기면 리액트에서 totalPages, content 등을 다 쓸 수 있음
        return new ResponseEntity<>(orderHistDtoList, HttpStatus.OK);
    }

    //취소니까 Delete. Delete는 리소스를 제거한다라는 의미인데 우리는 실제로 DB에서 제거하지는 않고 상태만 Cancel로 변경
    //이걸 soft delete라고 함.
    @DeleteMapping("/{orderId}")
    public ResponseEntity deleteOrder(@PathVariable("orderId") Long orderId,
                                      Principal principal) {

        // 이름은 cancelOrder지만 DELETE 요청을 처리함
        orderService.cancelOrder(orderId, principal.getName());

        return ResponseEntity.ok(orderId);
    }

}








