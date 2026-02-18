package com.jpa.market.mapper;

import com.jpa.market.dto.ItemImgDto;
import com.jpa.market.entity.ItemImg;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

//이 인터페이스의 역할 : ItemImg 엔티티 ↔ ItemImgDto 변환 전담
//이게 있어야만 스프링의 @Service나 @Controller에서 @Autowired로 이 매퍼를 불러올 수 있음
@Mapper(componentModel = "spring")
public interface ItemImgMapper {
    // 1. 엔티티 -> DTO 변환
    ItemImgDto entityToDto(ItemImg itemImg);

    // 2. DTO -> 엔티티 변환
    @Mapping(target = "id", ignore = true)     // DB가 자동 생성하므로 무시
    @Mapping(target = "item", ignore = true)   // 연관 관계는 서비스에서 따로 설정
    ItemImg dtoToEntity(ItemImgDto itemImgDto);
}
