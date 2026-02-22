package com.jpa.market.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    //**"우리 스프링 부트 앱이 내 AWS 계정의 S3 버킷에 접속할 수 있도록 통행증을 만드는 설정"**
    @Bean
    public S3Client s3Client() {
        //S3Client 객체 설정하기
        return S3Client.builder()
                .region(Region.of(region))  //리전 설정(서울)
                .credentialsProvider(   //인증절차
                        //내가 입력한 키를 사용해서 인증하도록 함.
                        StaticCredentialsProvider.create(
                                //IAM사용자의 ID와 pw 담기
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }
}
