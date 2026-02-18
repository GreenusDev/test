package com.jpa.market.config.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration //개발자가 작성한 클래스를 bean으로 등록하고자 할 때 사용
//WebMvcConfigurer: Spring MVC구성을 사용자가 변경할 수 있도록 메서드를 제공함
public class WebMvcConfig implements WebMvcConfigurer {

    //application.properties에 설정한 "uploadPath"의 값을 읽어옴
    @Value("${file.upload.uploadPath}")
    String uploadPath;

    @Override
    //정적 리소스(css, javascript, 이미지)에 대한 요청을 처리하는 방법을 오버라이드함
    //메서드의 역할 : “특정 URL 패턴이 오면 이걸 컨트롤러 말고 파일 시스템에서 찾아라”
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //브라우저에 입력하는 url이 /images로 시작하는 경우
        //uploadPath에 설정한 폴더를 기준으로 파일을 읽어오도록 함

        //웹브라우저에서는 img 태그의 이미지를 서버로 요청을할텐데,
        //WebMvcConfig에서 /images/** 경로로 들어올 경우
        //로컬의 upload Path 기준으로 이미지 파일을 읽어서
        //웹브라우저쪽으로 전달해주는 설정이라고봐주시면됩니다.
        registry.addResourceHandler("/img/**")

                //컴퓨터에 저장된 파일을 읽어올 root경로를 설정함
                .addResourceLocations(uploadPath);
    }
}