package com.jpa.market.controller;

import com.jpa.market.dto.ItemAdminListDto;
import com.jpa.market.dto.ItemFormDto;
import com.jpa.market.dto.ItemSearchDto;
import com.jpa.market.entity.Item;
import com.jpa.market.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController // @Controller + @ResponseBody (JSON 응답을 위해 필수)
@RequestMapping("/api") // API 경로임을 명시적으로 표현
@RequiredArgsConstructor
public class ItemController {
    //추가
    private final ItemService itemService;

    //권한 확인을 위한 페이지라서 이후에 삭제예정(화면 띄우는건 리액트에서 처리)
    //아이템은 관리자 아니여도 확인할 수 있어야하니까 필요할때마다 /admin 넣어주기
    @GetMapping(value = "/admin/items/new")
    public Map<String, Object> itemForm(){
        // 이제 HTML 파일을 리턴하는게 아니라, 필요한 데이터나 상태를 JSON으로 보냄
        Map<String, Object> response = new HashMap<>();
        response.put("message", "상품 등록 권한 확인 완료");
        response.put("status", "success");

        return response; // 리액트에게 { "message": "...", "status": "..." } 가 전달됨
    }

    // 리액트에서 상품 등록 버튼을 눌렀을 때 호출됨
    @PostMapping("/admin/items")
    public ResponseEntity itemNew(@Valid @RequestPart("itemCreateDto") ItemFormDto itemFormDto,
                                  @RequestPart("itemImgFile") List<MultipartFile> itemImgFileList) throws Exception {
//        try {
            Long itemId = itemService.saveItem(itemFormDto, itemImgFileList);
            return ResponseEntity.ok(itemId);

//        } catch (IllegalArgumentException e) {
////             "첫 번째 이미지는 필수입니다" 메시지를 400 에러와 함께 보냄
////             우리가 의도적으로 던진 에러 (이미지 없음 등) -> 400 에러
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
////            INTERNAL_SERVER_ERROR : 우리가 예상치 못한 찐 에러 (시스템 장애 등) -> 500 에러
////            "아, 내 프론트 코드는 문제가 없는데 서버 쪽 자바 코드가 실행되다가 에러가 났구나.
////             백엔드 개발자한테 로그 확인해달라고 해야지!"
//            return new ResponseEntity<>("상품 등록 중 에러가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
    }

    // 상품 상세 조회 (수정 페이지용 GET)
    //? : 아직 무슨 타입이 올지 정확히 모르겠지만, 일단 다 받아줄게
    //성공 시: ItemFormDto 객체를 JSON으로 변환해서 보냄
    //실패 시: "에러 발생" 같은 String 메시지를 보냄 둘 다 가능해집니다.
    @GetMapping("/items/{itemId}")
    public ResponseEntity<?> getItemDetail(@PathVariable("itemId") Long itemId) {
//        try {
            ItemFormDto itemFormDto = itemService.getItemDetail(itemId);
            return ResponseEntity.ok(itemFormDto);

//        } catch (EntityNotFoundException e) {
//            // 상품이 없을 경우 404 에러
//            return new ResponseEntity<>("상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
//        }
    }


    //일부 수정은 Patch를 쓰는게 더 좋긴한데, 우리는 파일 업로드도 해야해서
    //오래된 라이브러리나 브라우저에서
    // PATCH : multipart/form-data 처리에 버그가 많아서 사용하지 않음.
    //PUT : 리소스를 완전히 교체. 근데 상품 수정은 보통 일부수정이라 의미가 맞지 않음
    //결론은 파일을 포함해서 수정할때는 그냥 POST 쓰는게 젤 좋음.
    //json만 수정하거나 단순히 상태값을 변경할때는 PATCH 쓰기!
    @PostMapping("/admin/items/{itemId}")
    public ResponseEntity<?> itemUpdate(@PathVariable("itemId") Long itemId, // 1. PathVariable로 받기,
                                        @Valid @RequestPart("itemFormDto") ItemFormDto itemFormDto,
                                        @RequestPart("itemImgFile") List<MultipartFile> itemImgFileList) throws Exception{

        // 1. URL로 넘어온 ID를 DTO에 명시적으로 세팅
        //리액트(프론트엔드)에서 실수로 JSON 바디에 id를 안 담거나 잘못된 ID를 담을 수 있습니다.
        // 하지만 URL 주소는 명확하죠. 그래서 컨트롤러에서 URL에 있는 ID를 DTO에 넣어주는 것이 더 안전
        //URL을 기준으로 삼는 것이 REST API의 관례
        itemFormDto.setId(itemId);

        // 2. 서비스 호출 (예외 발생 시 핸들러가 처리함)
        itemService.updateItem(itemFormDto, itemImgFileList);

        return ResponseEntity.ok(itemFormDto.getId());
    }

    //상품 관리 페이지 확인시 url을
    //페이지번호가 있는경우, 없는 경우로 두가지 유형으로 설정
    @GetMapping(value= {"/admin/items", "/admin/items/{page}"})
    public ResponseEntity<Page<ItemAdminListDto>> itemManage(ItemSearchDto itemSearchDto,
                                                             //@PathVariable : url 경로{page}의 값을 받아옴
                                                             //Optional : 선택적으로 존재할 수 있음.
                                                             @PathVariable("page") Optional<Integer> page) {

        // 리액트에서 페이지 번호는 보통 1부터 시작하므로,
        // 서버에서 (page - 1) 처리를 해주는 것이 일반적이지만
        // 여기서는 0부터 시작하는 파라미터 기준으로 작성되었습니다.
        Pageable pageable = PageRequest.of(page.orElse(0), 5);

        //조회할 조건과 페이지에 대한 정보를 매개변수로 넘겨서 page객체 반환받음
        Page<ItemAdminListDto> items = itemService.getAdminItemPage(itemSearchDto, pageable);

        // 3. 리액트로 데이터와 상태 코드 반환
        return ResponseEntity.ok(items);

    }

}










