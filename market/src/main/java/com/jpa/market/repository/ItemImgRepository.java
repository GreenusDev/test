package com.jpa.market.repository;

import com.jpa.market.entity.Item;
import com.jpa.market.entity.ItemImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemImgRepository  extends JpaRepository<ItemImg, Long> {
    // 상품 수정 페이지에서 기존 이미지들을 불러올 때 사용합니다.
    List<ItemImg> findByItemIdOrderByIdAsc(Long itemId);

    // 상품 ID와 대표 이미지 여부(Y)를 조건으로 이미지 정보 조회
    // 서비스에서 itemImgRepository.findByItemIdAndRepimgYn(itemId, "Y") 로 사용합니다.
    ItemImg findByItemIdAndRepImgYn(Long itemId, String repImgYn);
}
