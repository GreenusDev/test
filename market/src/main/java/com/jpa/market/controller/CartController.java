package com.jpa.market.controller;

import com.jpa.market.dto.CartDetailDto;
import com.jpa.market.dto.CartItemDto;
import com.jpa.market.dto.CartOrderDto;
import com.jpa.market.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
// 기본적으로 복수로 설정. '내 것 하나'면 단수
//cart = "현재 로그인한 사용자의 장바구니 하나"
@RequestMapping("/api/cart")

public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<?> addCartItem(@RequestBody @Valid CartItemDto cartItemDto,
                                     Principal principal) {

        Long cartItemId = cartService.addCart(cartItemDto,  principal.getName());

        return ResponseEntity.ok(cartItemId);
    }

    @GetMapping
    public ResponseEntity<?> getCartList(Principal principal) {
        // principal은 시큐리티 설정 덕분에 무조건 보호됩니다.
        List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName());

        return ResponseEntity.ok(cartDetailList);
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable("cartItemId") Long cartItemId,
                                            @RequestParam("count") int count,
                                            Principal principal) {

        // 2. 서비스 호출
        // - cartItemId: 수정할 대상
        // - count: 변경할 수량
        // - principal.getName(): "열쇠(검증값)"로 사용될 로그인 ID
        cartService.updateCartItemCount(cartItemId, count, principal.getName());

        return ResponseEntity.ok(cartItemId);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> deleteCartItem(@PathVariable("cartItemId") Long cartItemId,
                                            Principal principal) {

        // 서비스 호출 (ID와 로그인 정보를 넘김)
        cartService.deleteCartItem(cartItemId, principal.getName());

        return ResponseEntity.ok(cartItemId);
    }

    /**
     * 장바구니 상품 주문 API
     * 리액트에서 선택된 장바구니 ID 리스트를 받아 주문을 생성합니다.
     */
    @PostMapping("/order")
    public ResponseEntity<?> orderCartItem(@RequestBody CartOrderDto cartOrderDto,
                                           Principal principal) {

        // 2. 서비스 호출 (주문 생성 + 장바구니 비우기)
        // 결과값으로 생성된 주문 ID를 받습니다.
        Long orderId = cartService.orderCartItem(cartOrderDto.getCartItemIds(), principal.getName());

        // 3. 성공 시 주문 번호 반환
        return ResponseEntity.ok(orderId);
    }
}
