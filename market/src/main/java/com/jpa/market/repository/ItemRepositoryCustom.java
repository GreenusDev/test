package com.jpa.market.repository;

import com.jpa.market.dto.ItemAdminListDto;
import com.jpa.market.dto.ItemSearchDto;
import com.jpa.market.dto.MainItemDto;
import com.jpa.market.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryCustom {

    //Page<Item>: 단순한 리스트(List)가 아니라,
    //      현재 페이지 데이터 + 전체 페이지 수 + 다음 페이지 여부 등 페이징에 필요한
    //      모든 정보를 한 번에 담아서 리액트(프론트엔드)로 보내줍니다.
    //ItemSearchDto: 앞서 만드신 검색 조건들(날짜, 판매상태, 검색어 등)이 담긴 주머니입니다.
    //      이 안의 값을 보고 쿼리를 결정합니다.
    //Pageable: Spring Data JPA에서 제공하는 페이징(Paging)과 정렬(Sorting) 정보를 담는 인터페이스
    //      리액트에서 보낸 "몇 번째 페이지를 보고 싶은지",
    //      "한 페이지에 몇 개씩 보여줄지"에 대한 설정값입니다.
    Page<ItemAdminListDto> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);

    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable);

}
