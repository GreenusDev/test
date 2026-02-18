package com.jpa.market.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Service
public class FileService {

    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception{

        // UUID를 생성하여 uuid 변수에 할당합니다.
        // 이는 서로 다른 개체들을 구별하기위해  고유한 파일 이름을 생성하기 위해 사용됩니다.
        UUID uuid = UUID.randomUUID();

        //originalFileName에서 마지막 점(.) 이후의 문자열을 추출하여 확장자를 가져옵니다.
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

        //UUID와 확장자를 결합하여 저장될 파일 이름을 생성합니다.
        String savedFileName = uuid.toString() + extension;

        //파일이 업로드될 전체 경로를 생성합니다.
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;

        //일 출력 스트림을 생성합니다.
        //파일 경로에 해당하는 파일을 생성하거나 덮어씁니다.
        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);

        //fileData의 내용을 파일에 기록합니다.
        fos.write(fileData);

        //파일 출력 스트림을 닫습니다.
        fos.close();

        //저장된 파일 이름을 반환합니다.
        return savedFileName;
    }

    public void deleteFile(String filePath) throws Exception{

        //주어진 filePath에 해당하는 파일을 나타내는 File 객체를 생성합니다.
        File deleteFile = new File(filePath);

        //삭제할 파일이 실제로 존재하는지 확인합니다.
        if(deleteFile.exists()) {

            // 파일을 삭제합니다.
            deleteFile.delete();

            //파일 삭제에 성공한 경우 로그를 출력합니다.
            System.out.println("파일을 삭제하였습니다.");
        } else {
            System.out.println("파일이 존재하지 않습니다.");
        }
    }

}