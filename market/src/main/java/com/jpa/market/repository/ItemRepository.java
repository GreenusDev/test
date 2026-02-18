package com.jpa.market.repository;

import com.jpa.market.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item>, ItemRepositoryCustom {

    //상품명 검색
    List<Item> findByItemName(String itemName);

    //상품명 또는 상품디테일로 검색
    List<Item> findByItemNameOrItemDetail(String itemName, String itemDetail);

    //특정 가격 이하의 상품
    List<Item> findByPriceLessThan(Integer price);

    //특정 가격 이하의 상품을 내림차순 정렬
    List<Item> findByPriceLessThanOrderByPriceDesc(Integer price);

//    @Query("select i from Item i where i.itemDetail like " +
//            "%?1% order by i.price desc")
//    List<Item> findByItemDetail(String itemDetail);

    @Query("select i from Item i where i.itemDetail like " +
            "%:itemDetail% order by i.price desc")
    List<Item> findByItemDetail(@Param("itemDetail") String itemDetail);

    @Query(value="select * from item i where i.item_detail like " +
            "%:itemDetail% order by i.price desc", nativeQuery = true)
    List<Item> findByItemDetailByNative(@Param("itemDetail") String itemDetail);
}















