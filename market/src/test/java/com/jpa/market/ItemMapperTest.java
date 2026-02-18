package com.jpa.market;

import com.jpa.market.dto.ItemFormDto;
import com.jpa.market.dto.ItemImgDto;
import com.jpa.market.entity.Item;
import com.jpa.market.mapper.ItemMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemMapperTest {

    @Autowired
    ItemMapper itemMapper;

    @Test
    @DisplayName("상품(Item)과 이미지 리스트가 한꺼번에 매핑되는지 테스트")
    void itemMappingTest() {
        // 1. Given: 테스트용 DTO 데이터 준비
        ItemFormDto dto = new ItemFormDto();
        dto.setItemName("테스트 상품");
        dto.setPrice(10000);

        //이미지 저장
        //실제 이미지가 아니라서 임의로 데이터를 넣음
        ItemImgDto imgDto = new ItemImgDto();
        imgDto.setOriImgName("test.jpg");
        imgDto.setImgUrl("/images/test.jpg");

        dto.getItemImgDtoList().add(imgDto); // DTO의 리스트에 이미지 추가

        // 2. When: DTO -> Entity 변환 (ItemMapper 사용)
        Item item = itemMapper.dtoToEntity(dto);

        // 3. Then: 검증
        assertThat(item.getItemName()).isEqualTo(dto.getItemName());
        assertThat(item.getPrice()).isEqualTo(dto.getPrice());

        // 리스트 매핑 검증 (가장 중요한 부분!)
        assertThat(item.getItemImgs()).isNotNull();
        assertThat(item.getItemImgs().size()).isEqualTo(1);
        assertThat(item.getItemImgs().get(0).getOriImgName()).isEqualTo("test.jpg");

        System.out.println("변환된 상품명: " + item.getItemName());
        System.out.println("변환된 이미지 개수: " + item.getItemImgs().size());
    }
}
