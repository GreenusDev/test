package com.jpa.market.mapper;

import com.jpa.market.dto.OrderItemDto;
import com.jpa.market.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    // 1. OrderItem 엔티티 -> OrderItemDto (조회용)
    //orderitem에 있는 item의 itemname을 가져와서 orderItemdto의 itemname에 넣어라
    @Mapping(source = "orderItem.item.itemName", target = "itemName")
    // orderItem에는 imgUrl이 없으므로 따로 받아와서 변환하기
    @Mapping(source = "imgUrl", target = "imgUrl")
    OrderItemDto entityToDto(OrderItem orderItem, String imgUrl);

    // 2. OrderItemDto -> OrderItem 엔티티
    //**"주문 상품은 단순한 값 복사가 아니라,
    // 서버의 비즈니스 로직(재고 차감, 가격 계산 등)을 거쳐서 탄생해야 하는 존재이기 때문"에
    //createOrderItem 정적 메서드에서 이미 로직을 처리하는게 옮음.
    // 그래서 이게 없어도 상관 없음.

    //화면의 데이터를 그대로 DB에 옮기기만 하면 되는 단순한 작업은 MapStruct가 효율적
    //재고 확인, 가격 계산, 상태 변경 등 '검사'와 '연산'이 필요한 탄생 과정은 **정적 메서드(create...)**가 안전
    //검사를 통해서 처리해야 한다면 정적메서드를 이용하면 됨.

    //지금 여기서는 상품의 금액을 그냥 넣는게 아니라 엔티티에서 상품*가격으로 넣어야하고,
    //주문갯수가 1개 이상임을 확인해야 하는 등의 필수 로직이 있으므로 create통해서 하는게 좋음.
    OrderItem dtoToEntity(OrderItemDto orderItemDto);
}
