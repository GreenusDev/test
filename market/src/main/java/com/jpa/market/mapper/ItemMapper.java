package com.jpa.market.mapper;

import com.jpa.market.dto.ItemFormDto;
import com.jpa.market.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


// 중요: uses에 ItemImgMapper를 넣어줘야 리스트 변환을 알아서 맡깁니다.
//이게 없으면 MapStruct는 리스트 안에 있는 객체들을 어떻게 변환해야 할지 몰라서 에러를 냅니다.
// "이미지 변환은 네 친구(ItemImgMapper)한테 물어봐!"라고 가이드를 주는 거예요.
@Mapper(componentModel = "spring", uses = {ItemImgMapper.class})
public interface ItemMapper {

    // 1. Item 엔티티 -> ItemCreateDto (조회할 때 사용)
    // Item 안의 List<ItemImg>가 DTO의 List<ItemImgDto>로 자동 변환됨
    @Mapping(source = "itemImgs", target = "itemImgDtoList")
    ItemFormDto entityToDto(Item item);

    // 2. ItemCreateDto -> Item 엔티티  (저장할 때 사용)
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "itemImgDtoList", target = "itemImgs")
    Item dtoToEntity(ItemFormDto itemFormDto);
}