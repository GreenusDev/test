package com.jpa.market;


import com.jpa.market.constant.ItemSellStatus;
import com.jpa.market.dto.ItemFormDto;
import com.jpa.market.entity.Item;
import com.jpa.market.entity.QItem;
import com.jpa.market.repository.ItemRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@SpringBootTest
public class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    @DisplayName("상품 저장 테스트")
    public void createItemTest(){
        // 1. DTO 생성 (리액트에서 보낸 JSON 데이터를 객체화했다고 가정)
        ItemFormDto itemFormDto = new ItemFormDto();

        itemFormDto.setItemName("테스트 상품");
        itemFormDto.setPrice(10000);
        itemFormDto.setStockNumber(100);
        itemFormDto.setItemDetail("테스트 상품 상세 설명");
        itemFormDto.setItemSellStatus(ItemSellStatus.SELL);

        // 2. 정적 메서드로 엔티티 생성
        Item item = Item.createItem(itemFormDto);

        // 3. 리포지토리에 저장
        Item savedItem = itemRepository.save(item);

        // 4. 검증 (출력으로 확인)
        System.out.println("저장된 상품 정보: " + savedItem.toString());
    }



    public void createItemList() {
        for (int i = 1; i <= 10; i++) {
            ItemFormDto itemFormDto = new ItemFormDto();

            itemFormDto.setItemName("테스트 상품" + i);
            itemFormDto.setPrice(10000 + i);
            itemFormDto.setStockNumber(100 + i);
            itemFormDto.setItemDetail("테스트 상품 상세 설명" + i);
            itemFormDto.setItemSellStatus(ItemSellStatus.SELL);

            Item item = Item.createItem(itemFormDto);

            itemRepository.save(item);
        }
    }

    public void createItemList2() {
        for (int i = 1; i <= 10; i++) {
            ItemFormDto itemFormDto = new ItemFormDto();

            itemFormDto.setItemName("테스트 상품" + i);
            itemFormDto.setPrice(10000 + i);
            itemFormDto.setStockNumber(100 + i);
            itemFormDto.setItemDetail("테스트 상품 상세 설명" + i);
            itemFormDto.setItemSellStatus(i % 2 == 0? ItemSellStatus.SELL : ItemSellStatus.SOLD_OUT);

            Item item = Item.createItem(itemFormDto);

            itemRepository.save(item);

        }
    }

    @Test
    @DisplayName("상품명 조회 테스트")
    public void findByItemNameTest() {
        this.createItemList();

        List<Item> itemList = itemRepository.findByItemName("테스트 상품1");

        for (Item item : itemList) {
            System.out.println(item.toString());
        }

        // for문 이렇게 줄여서 사용 가능
        //itemList.forEach(System.out::println);
    }

    @Test
    @DisplayName("상품명, 상품상세설명 or 테스트")
    public void findByItemNmOrItemDetailTest() {
        this.createItemList(); // 10개의 테스트 상품 생성

        List<Item> itemList = itemRepository.findByItemNameOrItemDetail("테스트 상품1", "테스트 상품 상세 설명5");

        itemList.forEach(System.out::println);
    }

    @Test
    @DisplayName("가격 LessThan 테스트")
    public void findByPriceLessThanTest(){
        // 1. 데이터 생성 (10001원 ~ 10010원까지 저장됨)
        this.createItemList();

        // 2. 10005원보다 작은 상품 조회 (10001, 10002, 10003, 10004 예상)
        List<Item> itemList = itemRepository.findByPriceLessThan(10005);

        // 3. 결과 출력
        itemList.forEach(System.out::println);
    }

    @Test
    @DisplayName("가격 내림차순 정렬 테스트")
    public void findByPriceLessThanOrderByPriceDescTest(){
        this.createItemList();

        List<Item> itemList = itemRepository.findByPriceLessThanOrderByPriceDesc(10005);

        // 3. 결과 출력
        itemList.forEach(System.out::println);
    }

    @Test
    @DisplayName("@Query를 이용한 상품 조회 테스트")
    public void findByItemDetailTest(){
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemDetail("테스트 상품 상세 설명");

        itemList.forEach(System.out::println);
    }

    @Test
    @DisplayName("@Query를 이용한 상품 조회 테스트")
    public void findByItemDetailByNativeTest(){
        this.createItemList();
        List<Item> itemList = itemRepository.findByItemDetailByNative("테스트 상품 상세 설명");
        for(Item item : itemList){
            System.out.println(item.toString());
        }
    }

    @Test
    @DisplayName("Querydsl 조회 테스트1")
    public void queryDslTest(){
        this.createItemList();

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QItem qItem = QItem.item;

        //방법 1
        //JPAQuery 만들고 결과를 List에 저장
        JPAQuery<Item> query  = queryFactory.selectFrom(qItem)
                                            .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL)
                                                    .and(qItem.itemDetail.contains("테스트 상품 상세 설명")))
                                            .orderBy(qItem.price.desc());

        List<Item> itemList = query.fetch();

        // 방법2
        //첨부터 List에 저장
//        List<Item> query = queryFactory.selectFrom(qItem)
//                                            .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL)
//                                                    .and(qItem.itemDetail.contains("테스트 상품 상세 설명")))
//                                            .orderBy(qItem.price.desc())
//                                            .fetch();

        for(Item item : itemList){
            System.out.println(item.toString());
        }
    }

    @Test
    @DisplayName("상품 Querydsl 조회 테스트 2 - 리팩토링")
    public void queryDslTest2() {
        createItemList2();

        QItem item = QItem.item;

        // BooleanBuilder로 조건 설정
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        String itemDetail = "테스트 상품 상세 설명";
        int price = 10003;
        String itemSellStat = "SELL";

        booleanBuilder
                .and(item.itemDetail.contains(itemDetail))
                .and(item.price.gt(price));

        if("SELL".equals(itemSellStat)) {
            booleanBuilder.and(item.itemSellStatus.eq(ItemSellStatus.SELL));
        }

        //PageRequest.of(조회할 페이지번호, 한 페이지당 조회할 데이터의 개수)
        // 페이징 설정 (0페이지, 5개)
        Pageable pageable = PageRequest.of(0, 5);

        // 조회
        Page<Item> itemPagingResult = itemRepository.findAll(booleanBuilder, pageable);

        System.out.println("total elements : " + itemPagingResult.getTotalElements());

        // 결과 출력
        List<Item> resultItemList = itemPagingResult.getContent();
        for(Item resultItem: resultItemList){
            System.out.println(resultItem.toString());
        }
    }

}
