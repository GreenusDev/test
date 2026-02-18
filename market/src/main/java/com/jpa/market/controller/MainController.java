package com.jpa.market.controller;

import com.jpa.market.dto.ItemSearchDto;
import com.jpa.market.dto.MainItemDto;
import com.jpa.market.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

//    @GetMapping
//    public ResponseEntity<Page<MainItemDto>> getMainPage(ItemSearchDto itemSearchDto,
//                                                         @RequestParam("page") Optional<Integer> page) {
//
//        // 메인 화면은 보통 한 페이지에 6개나 8개씩 격자(Grid)로 보여줍니다.
//        Pageable pageable = PageRequest.of(page.orElse(0), 6);
//        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);
//
//        return ResponseEntity.ok(items);
//    }

    @GetMapping // 이제 주소는 /api/main 이 됩니다.
    public ResponseEntity<Map<String, Object>> getMainPage(ItemSearchDto itemSearchDto,
                                                           @RequestParam("page") Optional<Integer> page) {

        Pageable pageable = PageRequest.of(page.orElse(0), 6);
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        Map<String, Object> response = new HashMap<>();

        // 1. 상품 관련 데이터
        response.put("items", items);
        response.put("maxPage", 5);

        // 2. 추가하고 싶은 데이터들 (필요할 때 바로 추가 가능)
        response.put("userRole", "USER"); // 예시: 권한 정보
        response.put("isEventActive", true); // 예시: 이벤트 진행 여부

        return ResponseEntity.ok(response);
    }
}
