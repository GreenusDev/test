package com.jpa.market.dto;

import com.jpa.market.constant.ItemSellStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemFormDto {
    private Long id;        //제품 수정할 때 사용

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String itemName;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    @NotBlank(message = "상품 상세는 필수 입력 값입니다.")
    private String itemDetail;

    @NotNull(message = "재고는 필수 입력 값입니다.")
    private Integer stockNumber;

    private ItemSellStatus itemSellStatus;

    // 수정 시 이미지 아이디를 저장하는 리스트 (추가!)
    //수정 페이지에서 사용자가 2번 이미지만 바꿨다고 가정해 봅시다.
    // 서버는 "전체 이미지 중 도대체 몇 번 PK(ID)를 가진 데이터를 새 파일로 교체해야 하는지" 알아야 합니다.
    // 프론트에서 수정 요청을 보낼때 이미지의 id를 가지고 어떤 이미지를 변경하는지 확인하기 위한것
    private List<Long> itemImgIds = new ArrayList<>();

    // --- 추가: 상품 저장 후 이미지 정보를 담을 리스트 ---
    // 실제 등록 시에는 파일(MultipartFile)을 담는 리스트가 따로 필요하겠지만,
    // 일단 데이터를 주고받는 구조에서는 이렇게 매핑됩니다.
    private List<ItemImgDto> itemImgDtoList = new ArrayList<>();

}
