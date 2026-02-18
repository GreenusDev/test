package com.jpa.market.service;

import com.jpa.market.dto.ItemImgDto;
import com.jpa.market.entity.Item;
import com.jpa.market.entity.ItemImg;
import com.jpa.market.repository.ItemImgRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

import static com.jpa.market.entity.QItem.item;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemImgService {

    @Value("${file.upload.itemImgLocation}")
    private String itemImgLocation;

    private final ItemImgRepository itemImgRepository;

    private final FileService fileService;

    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception{
        String oriImgName = itemImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl = "";

        //파일 업로드
        if(!StringUtils.isEmpty(oriImgName)){
            //사용자가 상품이미지를 등록했다면
            //저장할 경로, 파일명, 파일의 바이트 배열을 파입 업로드파마리터로
            //uploadFile 메서드를 호출함.
            //호출결과 로컬에 저장된 파일의 이름을 imgName 변수에 저장함
            imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());

            //저장한 상품 이미지를 불러올 경로를 설정
            imgUrl = "/img/item/" + imgName;
        }

        itemImg.updateItemImg(oriImgName, imgName, imgUrl, itemImg.getRepImgYn());
        itemImgRepository.save(itemImg);
    }

//    public void updateItemImg(Long itemImgId, MultipartFile itemImgFile, String repImgYn) throws Exception {
//        ItemImg savedItemImg = itemImgRepository.findById(itemImgId)
//                .orElseThrow(EntityNotFoundException::new);
//
//        // 1. 파일이 비어있지 않은 경우 -> 파일 정보 + 대표 여부 모두 업데이트
//        if (itemImgFile != null && !itemImgFile.isEmpty()) {
//            // 기존 파일 삭제 로직...
//            if (!StringUtils.isEmpty(savedItemImg.getImgName())) {
//                fileService.deleteFile(itemImgLocation + File.separator + savedItemImg.getImgName());
//            }
//
//            String oriImgName = itemImgFile.getOriginalFilename();
//            String imgName = fileService.uploadFile(itemImgLocation, oriImgName, itemImgFile.getBytes());
//            String imgUrl = "/img/item/" + imgName;
//
//            // 엔티티의 비즈니스 메서드 호출 (Setter 대신)
//            savedItemImg.updateItemImg(oriImgName, imgName, imgUrl, repImgYn);
//
//        } else {
//            // 2. 파일은 없지만(기존 이미지 유지) 대표 여부는 바뀔 수 있는 경우
//            // 예: 원래 2번이었던 사진을 1번(대표) 자리로 옮겼을 때 등
//            savedItemImg.updateRepImgYn(repImgYn);
//        }
//    }

    //이미지 삭제
    public void deleteItemImgs(Item item) throws Exception {
        // 1. 해당 상품에 연결된 모든 이미지 DB에서 조회
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(item.getId());

        for (ItemImg itemImg : itemImgList) {
            // 2. 서버 폴더의 실제 물리 파일 삭제
            if (!StringUtils.isEmpty(itemImg.getImgName())) {
                fileService.deleteFile(itemImgLocation + File.separator + itemImg.getImgName());
            }
            // 3. DB 레코드 삭제
            itemImgRepository.delete(itemImg);
        }
        // 영속성 컨텍스트를 비워주거나 플러시하여 즉시 반영 (선택사항)
        itemImgRepository.flush();
    }
}











