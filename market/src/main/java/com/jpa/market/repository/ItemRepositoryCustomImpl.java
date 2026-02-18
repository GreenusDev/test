package com.jpa.market.repository;

import com.jpa.market.constant.ItemSellStatus;
import com.jpa.market.dto.*;
import com.jpa.market.entity.Item;
import com.jpa.market.entity.QItem;
import com.jpa.market.entity.QItemImg;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

//ItemRepositoryCustom를 구현하는 인터페이스 작성
//클래스명 뒤에 반드시 Impl를 붙여야 정상적으로 동작함
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{

    //동적으로 쿼리를 생성하기 위해서 JPAQueryFactory 클래스를 사용함
    private JPAQueryFactory queryFactory;


    //JPAQueryFactory의 생성자로 EntityManager를 초기화함
    public ItemRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    //반환타입 BooleanExpression
    //			: Querydsl에서 조건을 표현하기 위해 사용되는 인터페이스
    //			: SQL의 where에 해당하는 부분


    //ItemSellStatus를 이용하여
    //상품 판매 상태 조건이 null이면(전체) null을 리턴
    //결과값이 null이면 해당 조건은 무시됨
    //판매 상태 조건이 null이 아니면 판매중 또는 품절상태에 해당하는 상품을 조회
    private BooleanExpression searchSellStatusEq (ItemSellStatus searchSellStatus) {
        return searchSellStatus == null ? null : QItem.item.itemSellStatus.eq(searchSellStatus);

    }

    //searchDateType값에 따라서 현재 날짜와 시간을 이전값으로 설정
    //해당 시간 이후로 등록된 상품만 조회하도록 함
    private BooleanExpression regDtsAfter(String searchDateType) {
        LocalDateTime dateTime = LocalDateTime.now();

        if(Objects.equals("all", searchDateType) || searchDateType == null) {
            return null;
        } else if (Objects.equals("1d", searchDateType)) {
            dateTime = dateTime.minusDays(1);
        } else if (Objects.equals("1w", searchDateType)) {
            dateTime = dateTime.minusWeeks(1);
        } else if (Objects.equals("1m", searchDateType)) {
            dateTime = dateTime.minusMonths(1);
        } else if (Objects.equals("6m", searchDateType)) {
            dateTime = dateTime.minusMonths(6);
        }

        //해당 시간 이후로 등록된 상품만 조회하도록 함
        return QItem.item.regTime.after(dateTime);
    }

    //검색어가 포함되어 있는 상품 또는 상품을 등록한 사람의 아이디를 조회하여
    //결과값 반환
    private BooleanExpression searchByLike(String searchBy, String searchQuery) {
        if(searchQuery == null || searchQuery.isEmpty()) {
            return null;
        }

        if(Objects.equals("itemName", searchBy)) {
            return QItem.item.itemName.contains(searchQuery);
        } else if (Objects.equals("createdBy", searchBy)) {
            return QItem.item.createdBy.contains(searchQuery);
        }

        return null;
    }


    @Override
    public Page<ItemAdminListDto> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        // 1. 실제 데이터 조회
        List<ItemAdminListDto> content = queryFactory
                .select(new QItemAdminListDto( // Q-Type 생성자를 사용하여 바로 DTO로 조회
                QItem.item.id,
                QItem.item.itemName,
                QItem.item.itemSellStatus,
                QItem.item.createdBy,
                QItem.item.regTime
                ))
                .from(QItem.item)
                .where(
                        regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery())
                )
                .orderBy(QItem.item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 카운트 쿼리 (별도로 분리)
        JPAQuery<Long> countQuery = queryFactory
                .select(QItem.item.count()) // count(item.id)와 동일
                .from(QItem.item)
                .where(
                        regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery())
                );
        // 3. PageableExecutionUtils를 사용하여 Page 객체 생성 (성능 최적화)
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        List<MainItemDto> content = queryFactory
                .select(
                        new QMainItemDto(
                                item.id,
                                item.itemName,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price)
                )
                .from(itemImg)  // 조건이 더 많은 쪽을  from에 작성
                .join(itemImg.item, item) // ItemImg와 Item 조인
                .where(itemImg.repImgYn.eq("Y")) // 대표 이미지만 불러옴
                .where(itemNameLike(itemSearchDto.getSearchQuery())) // 검색어 필터
                .orderBy(item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 개수 조회 (count 쿼리)
        long total = queryFactory
                .select(item.count())
                .from(itemImg)
                .join(itemImg.item, item)
                .where(itemImg.repImgYn.eq("Y"))
                .where(itemNameLike(itemSearchDto.getSearchQuery()))
                .fetchOne();

        // 카운트 쿼리는 생략 (기존 방식과 동일)
        return new PageImpl<>(content, pageable, total);
    }

    // 검색어 조건 메서드 추가
    private BooleanExpression itemNameLike(String searchQuery) {
        // StringUtils.isEmpty(searchQuery) 대신 직접 체크
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return null;
        }
        return QItem.item.itemName.contains(searchQuery);
    }

}
