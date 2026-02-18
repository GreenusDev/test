package com.jpa.market.mapper;

import com.jpa.market.dto.OrderHistDto;
import com.jpa.market.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(source = "id", target = "orderId")
    // LocalDateTime을 String으로 변환하는 설정
    @Mapping(source = "orderDate", target = "orderDate", dateFormat = "yyyy-MM-dd HH:mm")
    @Mapping(target = "orderItemDtoList", ignore = true)    //연관관계 제외
    OrderHistDto entityToDto(Order order);


    //Order는 단순한 복사로 만들 수 없는 객체입니다
    //사용자님이 작성하신 createOrder 정적 메서드를 보세요.
    // 이 안에는 아주 중요한 **'행위'**들이 들어있습니다.

    //양방향 연결 (addOrderItem):
    //      리스트에 아이템을 넣으면서 동시에 자식들에게 "내가 네 아빠야"라고 알려주는 복잡한 과정이 있습니다.
    //상태 결정 (OrderStatus.ORDER):
    //      주문이 생성될 때 기본 상태를 지정합니다.
    //시간 기록 (LocalDateTime.now()):
    //      주문이 들어온 '서버 시간'을 기록합니다.

    //만약 MapStruct의 dtoToEntity를 쓰면?
    // MapStruct는 그냥 DTO에 있는 값을 필드에 슥슥 복사만 합니다.

    // 그러면 addOrderItem 같은 편의 메서드가 실행되지 않아
    // DB에 외래키(order_id)가 null로 박히거나,
    // 주문 상태가 비어있는 치명적인 오류가 발생합니다.
}
