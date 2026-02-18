package com.jpa.market.service;


import com.jpa.market.dto.ItemAdminListDto;
import com.jpa.market.dto.ItemFormDto;
import com.jpa.market.dto.ItemSearchDto;
import com.jpa.market.dto.MainItemDto;
import com.jpa.market.entity.Item;
import com.jpa.market.entity.ItemImg;
import com.jpa.market.mapper.ItemMapper;
import com.jpa.market.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImgService itemImgService;
    private final ItemMapper itemMapper; // 1. MapStruct 매퍼 주입

    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception{

        // [추가] 첫 번째 이미지 필수 체크 (리액트에서 빈 리스트를 보낼 경우 방어)
        if(itemImgFileList == null || itemImgFileList.isEmpty() || itemImgFileList.get(0).isEmpty()){
            throw new IllegalArgumentException("첫 번째 상품 이미지는 필수 입력 값입니다.");
        }

        //상품 등록
        Item item = itemMapper.dtoToEntity(itemFormDto);
        itemRepository.save(item);

        //이미지 등록
        for(int i=0; i<itemImgFileList.size(); i++){
            MultipartFile itemImgFile = itemImgFileList.get(i);

            // 리액트에서 보낸 파일이 존재할 때만 엔티티 생성 및 저장
            if (itemImgFile != null && !itemImgFile.isEmpty()) {
                // Builder 패턴을 사용하여 ItemImg 객체 생성
                ItemImg itemImg = ItemImg.builder()
                        .item(item)
                        .repImgYn(i == 0 ? "Y" : "N")
                        .build();

                // ItemImgService 호출 (내부에서 updateItemImg 비즈니스 로직 실행)
                itemImgService.saveItemImg(itemImg, itemImgFile);
            }
        }

        return item.getId();
    }

    @Transactional(readOnly = true)
    public ItemFormDto getItemDetail(Long itemId) {
        // 1. 상품을 조회할 때 JPA의 Fetch 전략에 따라 연관된 이미지들도 함께 가져옵니다.
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("해당 상품을 찾을 수 없습니다. (ID: " + itemId + ")"));

        // 2. MapStruct가 Item 안의 List<ItemImg>를 ItemFormDto의 List<ItemImgDto>로
        // 알아서 변환해서 채워줍니다. (uses 설정 덕분!)
        return itemMapper.entityToDto(item);
    }

    /**
     * 상품 및 상품 이미지 수정 로직
     * @param itemFormDto : 리액트에서 전달된 수정된 상품 정보와 기존 이미지 ID 리스트
     * @param itemImgFileList : 리액트에서 전달된 이미지 파일 리스트 (수정 안된 칸은 빈 파일)
     */
    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {

        // 상품 수정 시에도 이미지가 비어있는지 체크하는 비즈니스 로직
        if (itemImgFileList == null || itemImgFileList.isEmpty() || itemImgFileList.get(0).isEmpty()) {
            throw new IllegalArgumentException("첫 번째 상품 이미지는 필수 입력 값입니다.");
        }

        // 1. 상품 기본 정보 수정
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("수정하려는 상품을 찾을 수 없습니다. (ID: " + itemFormDto.getId() + ")"));
        item.updateItem(itemFormDto);

        // 2. [핵심] 기존 이미지 싹 삭제 (물리 파일 + DB 데이터)
        itemImgService.deleteItemImgs(item);

        // 3. 새로 넘어온 파일 리스트를 다시 하나씩 저장 (Insert)
        for (int i = 0; i < itemImgFileList.size(); i++) {
            MultipartFile file = itemImgFileList.get(i);

            if (file != null && !file.isEmpty()) {
                // 새로운 엔티티 생성 (PK가 새로 따짐)
                ItemImg itemImg = ItemImg.builder()
                        .item(item)
                        .repImgYn(i == 0 ? "Y" : "N") // 첫 번째 사진을 대표 이미지로
                        .build();

                // saveItemImg는 기존에 쓰시던 '새로 저장' 로직 그대로 사용
                itemImgService.saveItemImg(itemImg, file);
            }
        }
        return item.getId();
    }

    //상품 조회 조건과 페이지 정보를 파라미터로 받아서 조회
    //데이터의 변경이 발생하지 않으므로 읽기전용으로 설정
    @Transactional(readOnly = true)
    public Page<ItemAdminListDto> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        // 리포지토리의 Querydsl custom 메서드 호출
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }
}












