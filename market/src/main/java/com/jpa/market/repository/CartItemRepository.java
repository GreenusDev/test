package com.jpa.market.repository;

import com.jpa.market.dto.CartDetailDto;
import com.jpa.market.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 1. 장바구니에 이미 담긴 상품인지 확인
    // 서비스에서 '장바구니 담기' 시 중복 체크를 위해 사용합니다.
    CartItem findByCartIdAndItemId(Long cartId, Long itemId);

    //일단 이렇게 먼저 쓰고 고칠꺼임!!!
    @Query("select new com.jpa.market.dto.CartDetailDto(" +
            "ci.id, i.itemName, i.price, ci.count, im.imgUrl) " +
            "from CartItem ci " +
            "join ci.item i " +
            "join i.itemImgs im " + // Item 엔티티의 이미지 리스트와 조인
            "where ci.cart.id = :cartId " +
            "and im.repImgYn = 'Y' " +   // 대표 이미지만!
            "order by ci.regTime desc")
    //JPQL에서 :이름 파라미터를 썼으면 메서드 파라미터에 @Param("이름") 반드시 필요
    //👉@Param("cartId"):  "JPQL의 :cartId ← 이 파라미터랑 연결해라"
    List<CartDetailDto> findCartDetailDtoList(@Param("cartId") Long cartId);
}


