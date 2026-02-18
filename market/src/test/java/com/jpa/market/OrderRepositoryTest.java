package com.jpa.market;

import com.jpa.market.constant.ItemSellStatus;
import com.jpa.market.dto.ItemFormDto;
import com.jpa.market.dto.MemberJoinDto;
import com.jpa.market.entity.Item;
import com.jpa.market.entity.Member;
import com.jpa.market.entity.Order;
import com.jpa.market.entity.OrderItem;
import com.jpa.market.repository.ItemRepository;
import com.jpa.market.repository.MemberRepository;
import com.jpa.market.repository.OrderItemRepository;
import com.jpa.market.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
//@Rollback(false) 어노테이션 있음 오류남
public class OrderRepositoryTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    //추가하기
    @Autowired
    OrderItemRepository orderItemRepository;

    @PersistenceContext
    EntityManager em;

    // Item 엔티티의 정적 메서드를 사용하거나, 없으면 이 도우미 메서드 사용
    public Item createItem() {
        // 1. 빌더를 사용하여 DTO 생성
        ItemFormDto itemFormDto = new ItemFormDto();

        itemFormDto.setItemName("테스트 상품");
        itemFormDto.setPrice(10000);
        itemFormDto.setStockNumber(100);
        itemFormDto.setItemDetail("테스트 상품 상세 설명");
        itemFormDto.setItemSellStatus(ItemSellStatus.SELL);

        return Item.createItem(itemFormDto);
    }

    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest() {
        // 1. Order 객체 생성 (Member는 일단 null)
        Order order = Order.createOrder(null, null);

        for(int i=0; i<3; i++){
            // 2. Item 생성 및 저장
            Item item = this.createItem();
            itemRepository.save(item);

            // 3. OrderItem 생성
            OrderItem orderItem = OrderItem.createOrderItem(item, 10);

            // 4. 연관관계 편의 메서드로 부모-자식 연결
            order.addOrderItem(orderItem);

            //order.getOrderItems().add(orderItem);
        }

        // 5. 부모만 저장 -> 자식까지 한꺼번에 저장되는지 확인
        orderRepository.saveAndFlush(order);
        em.clear();

        // 6. 검증
        Order savedOrder = orderRepository.findById(order.getId())
                .orElseThrow(EntityNotFoundException::new);

        assertEquals(3, savedOrder.getOrderItems().size());
    }

    //MemberRepository 테스트에서 복붙
    public MemberJoinDto createMember() {

        MemberJoinDto dto = new MemberJoinDto();
        dto.setLoginId("green123");
        dto.setPassword("12345678");
        dto.setName("김그린");
        dto.setEmail("green@naver.com");
        dto.setAddress("울산시 남구 삼산동");

        return dto;
    }

    // 2. DTO를 받아 엔티티로 변환하고 DB에 저장하는 메서드
    public Member saveMember() {
        MemberJoinDto memberJoinDto = this.createMember();

        // Member 엔티티에 만들어둔 정적 메서드 활용
        Member member = Member.createMember(memberJoinDto, passwordEncoder);

        return memberRepository.save(member);
    }


    // 테스트 코드 내부의 createOrder()
    public Order createOrder() {
        // DTO 기반으로 생성된 Member 저장
        Member member = this.saveMember();

        Order order = Order.createOrder(member, null);

        //주문 상품 3개 생성
        for(int i=0; i<3; i++) {
            Item item = createItem(); // 기존에 만드신 아이템 생성 메서드
            itemRepository.save(item);

            OrderItem orderItem = OrderItem.createOrderItem(item, 10);
            order.addOrderItem(orderItem);
        }

        return orderRepository.save(order);
    }

    @Test
    @DisplayName("고아객체 제거 테스트")
    public void orphanRemovalTest(){
        //주문 생성
        Order order = this.createOrder();

        //Order 객체의 orderItems 리스트에서
        //첫 번째 OrderItem을 제거
        //아직 DB에는 아무 일도 안 일어남
        order.getOrderItems().remove(0);

        //영속성 컨텍스트의 변경 내용을 DB에 반영
        em.flush();
    }

    //추가하기
    @Test
    @DisplayName("지연 로딩 테스트")
    public void lazyLoadingTest(){
        Order order = this.createOrder();
        Long orderItemId = order.getOrderItems().get(0).getId();
        em.flush();
        em.clear();

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(EntityNotFoundException::new);

        //지연 로딩(LAZY)으로 설정된 연관 관계 데이터를 조회하면,
        //JPA는 진짜 엔티티 대신 가짜 객체를 채워넣습니다. 이를 **프록시(Proxy)**라고 합니다.
        System.out.println("Order class : " + orderItem.getOrder().getClass());

        //추가하기
        System.out.println("===========================");
        orderItem.getOrder().getOrderDate();
        System.out.println("===========================");

        System.out.println("Order class : " + orderItem.getOrder().getClass());
        System.out.println("Order Date: " + orderItem.getOrder().getOrderDate());
    }



}
