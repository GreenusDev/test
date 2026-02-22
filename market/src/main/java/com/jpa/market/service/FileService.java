package com.jpa.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor    //추가
public class FileService {

    //추가
    private final S3Client s3Client;

    //추가
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    //상황에 따라 다른 폴더에 저장하도록 설정
    // folder 파라미터를 추가하여 "items" 또는 "users" 등을 전달받음
    public String uploadFile(String folder, //변경
                             String originalFileName,
                             byte[] fileData) throws Exception {

        //uuid를 이용하여 고유한 파일 이름을 생성하기 위해 사용
        UUID uuid = UUID.randomUUID();

        //확장자 추출
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

        //uuid와 확장자를 결합하여 저장할 파일명 생성
        String savedFileName = uuid.toString() + extension;

        // S3 내의 최종 경로 (예: items/uuid.jpg)
        String key = folder + "/" + savedFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("image/jpeg") // 실제론 파일 타입에 맞춰 동적 세팅 가능
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileData));

        //메서드 추가
        return getUploadUrl(key); // (삭제를 위해 key는 별도로 관리하거나 DB의 imgName에 저장하세요)

    }

    //등록된 파일 삭제
    public void deleteFile(String key) throws Exception {

        //다지우고 새로쓰기

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key) // S3 내의 경로 (folder/savedFileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        System.out.println(key + " : S3에서 파일 삭제 완료");

    }

    public String getUploadUrl(String key) {

        //문자열 조합하기 위해서 %s 사용
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }
}