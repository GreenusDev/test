package com.jpa.market.service;

import com.jpa.market.dto.CartDetailDto;
import com.jpa.market.dto.CartItemDto;
import com.jpa.market.dto.OrderDto;
import com.jpa.market.entity.Cart;
import com.jpa.market.entity.CartItem;
import com.jpa.market.entity.Item;
import com.jpa.market.entity.Member;
import com.jpa.market.repository.CartItemRepository;
import com.jpa.market.repository.CartRepository;
import com.jpa.market.repository.ItemRepository;
import com.jpa.market.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    //의존성 주입
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    //**"남의 집 일"**을 시킬 때는 그 집의 대리인인 **서비스(Service)**를 부르고,
    // **"내 집(내 엔티티) 관련 일"**을 할 때는 직접 창고인 **레포지토리(Repository)**
    private final OrderService orderService;    //주문과 관련된 복잡한 규칙들을 처리하기위해 service 주입

    public Long addCart(CartItemDto cartItemDto, String loginId) {

        // 1. 상품 조회
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 2. 회원 조회
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 3. 현재 로그인한 회원의 장바구니 엔티티 조회
        Cart cart = cartRepository.findByMemberId(member.getId());

        // 4. 장바구니가 없으면 새로 생성
        if (cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        // 5. 현재 상품이 장바구니에 이미 들어있는지 확인
        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        if (savedCartItem != null) {
            // 6-1. 이미 있으면 수량만 증가
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        } else {
            // 6-2. 없으면 CartItem 생성 후 저장
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String loginId) {

        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        // 1. 현재 로그인한 회원 조회
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 2. 해당 회원의 장바구니 조회
        Cart cart = cartRepository.findByMemberId(member.getId());

        // 3. 장바구니가 비어있으면 빈 리스트 반환
        if(cart == null){
            return cartDetailDtoList;
        }

        // 4. 장바구니에 담긴 상품 목록 조회
        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());

        return cartDetailDtoList;
    }

    // 수량 업데이트
    public void updateCartItemCount(Long cartItemId, int count, String loginId) {
        // 1. 수량 검증 (비즈니스 규칙)
        //dto로 검사할수도 있는데 그러면 불필요한 itemId도 같이 실어서 보내야함.
        //수량만 변경하고 싶은데 상품id같이 같이 보내야하므로 따로 받아서 처리하고 여기서 유효성검사 실행
        if (count <= 0) {
            throw new IllegalArgumentException("최소 1개 이상의 수량을 입력해주세요.");
        }

        // 2. 상품 존재 확인
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("장바구니 상품을 찾을 수 없습니다."));

        // 3. 보안 체크: 주문 취소 때와 똑같이 loginId 비교
        if (!cartItem.getCart().getMember().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("주문 수정 권한이 없습니다.");
        }

        // 4. 업데이트
        cartItem.updateCount(count);
    }

    public void deleteCartItem(Long cartItemId, String loginId) {
        // 1. 삭제할 아이템 조회
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("삭제하려는 상품이 없습니다."));

        // 2. 보안 체크: 로그인한 사용자와 장바구니 주인이 같은지 확인
        if (!cartItem.getCart().getMember().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        // 3. 삭제 실행
        cartItemRepository.delete(cartItem);
    }

    /**
     * 장바구니에서 선택된 여러 상품들을 실제 주문 데이터로 전환하는 핵심 로직
     * @param cartItemIds : 리액트(화면)에서 체크박스로 선택된 장바구니 아이템 번호들
     * @param loginId : 현재 로그인한 사용자 ID (보안 검증용)
     */
    public Long orderCartItem(List<Long> cartItemIds, String loginId) {
        // 1. 주문할 상품이 아예 선택되지 않았을 경우 서비스에서 차단
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품을 선택해주세요.");
        }


        // [준비 단계] OrderService에 넘겨줄 '순수 주문 정보' 바구니를 만듭니다.
        List<OrderDto> orderDtoList = new ArrayList<>();

        // --- [1단계: 검증 및 변환] ---
        // 화면에서 넘어온 '장바구니 번호'들만 가지고는 주문을 만들 수 없으므로,
        // 하나씩 꺼내서 진짜 주문에 필요한 정보(상품ID, 수량)를 추출합니다.
        for (Long cartItemId : cartItemIds) {

            // 1-1. DB에서 장바구니 아이템 정보 조회 (없으면 예외 발생)
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new EntityNotFoundException("장바구니 상품을 찾을 수 없습니다. ID: " + cartItemId));

            // 1-2. 권한 확인
            //장바구니에 10개를 주문하면 10개 다 내것이어야함.
            //그래서 반복문 안에서 권한 확인
            if (!cartItem.getCart().getMember().getLoginId().equals(loginId)) {
                throw new AccessDeniedException("해당 상품에 대한 주문 권한이 없습니다.");
            }

            // 1-3. 주문용 DTO로 변환
            // 장바구니 엔티티(CartItem)를 그대로 넘기지 않고,
            // 주문 로직에 꼭 필요한 정보인 [상품번호, 주문수량]만 쏙 뽑아서 DTO에 담습니다.
            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());

            orderDtoList.add(orderDto);
        }

        // --- [2단계: 실제 주문 처리] ---
        // 검증이 끝난 리스트를 가지고 OrderService의 다중 주문 메서드를 호출합니다.
        // 여기서 비로소 'Order' 테이블에 데이터가 쌓이고 'Item'의 재고가 줄어듭니다.
        // (이 과정에서 재고가 부족하면 @Transactional에 의해 전체 과정이 취소됩니다.)
        Long orderId = orderService.orderMultipleItems(orderDtoList, loginId);

        // --- [3단계: 장바구니 뒷정리] ---
        // 주문서가 성공적으로 써졌으니, 이제 장바구니에 담아뒀던 물건들은 비워줘야 합니다.
        // (보통 쇼핑몰에서 결제 완료 후 장바구니를 확인하면 물건이 사라지는 원리입니다.)
        for (Long cartItemId : cartItemIds) {
            // 이미 1단계에서 존재를 확인했으므로 바로 조회해서 삭제합니다.
            CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow();
            cartItemRepository.delete(cartItem);
        }

        // 최종적으로 생성된 주문 번호(ID)를 리턴합니다.
        return orderId;
    }
}
