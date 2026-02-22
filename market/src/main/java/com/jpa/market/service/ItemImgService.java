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
@Transactional
@RequiredArgsConstructor    //주요 필드에 대한 생성자를 자동으로 만들어주는 어노테이션
public class ItemImgService {

    //지우기(FileService에서 처리하고 폴더로 구분할 예정)
    //@Value("${file.upload.itemImgLocation}")
    //private String itemImgLocation;

    private final ItemImgRepository itemImgRepository;
    private final FileService fileService;

    //엔티티는 객체이므로 객체값만 바꾸면 DB값도 변경되므로 엔티티로 바로 사용함
    public void saveItemImg(ItemImg itemImg, MultipartFile itemImgFile) throws Exception {
        String oriImgName = itemImgFile.getOriginalFilename();
        String imgName = "";
        String imgUrl = "";

        //사용자가 파일을 등록했으면
        if (!StringUtils.isEmpty(oriImgName)) {
            // FileService가 내부적으로 bucket, region 정보를 사용해서
            // https://... 형태의 완벽한 URL을 만들어서 줍니다.
            imgUrl = fileService.uploadFile("items", oriImgName,
                    itemImgFile.getBytes());
            // 2. 받아온 전체 URL에서 S3 Key(items/uuid.jpg) 부분만 추출합니다.
            // .com/ 이후의 문자열을 잘라내어 imgName에 저장합니다 (삭제 시 활용).
            if (imgUrl.contains(".com/")) {
                imgName = imgUrl.substring(imgUrl.lastIndexOf(".com/") + 5);
            }

        }
        // 3. 엔티티 데이터 업데이트
        // imgName(Key), oriImgName(원본명), imgUrl(전체경로)를 각각 저장합니다.
        itemImg.updateItemImg(imgName, oriImgName, imgUrl, itemImg.getRepImgYn());

        itemImgRepository.save(itemImg);
    }


    //이미지 삭제
    public void deleteItemImg(Item item) throws Exception {
        // 해당 상품에 연결된 모든 이미지를 조회
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(item.getId());

        for (ItemImg itemImg : itemImgList) {

            // 2. DB의 imgName 컬럼에 저장된 S3 Key("items/uuid.jpg")를 꺼내옴
            String s3Key = itemImg.getImgName();

            //이미지를 실제 삭제 처리
            if (!StringUtils.isEmpty(s3Key))
                // FileService의 deleteFile은 key 하나만 받도록 만드셨으니 그대로 전달!
                fileService.deleteFile(s3Key);

            //db에서 삭제
            itemImgRepository.delete(itemImg);
        }
        itemImgRepository.flush();
    }
}











