package com.jpa.market.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.Length;

//Request DTO → 불변 필요 없음
//이건 회원가입을 요청하는 Dto라서 setter까지 추가
@Getter @Setter
public class MemberJoinDto {

    @NotBlank(message = "아이디는 필수입니다.")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Length(min = 8, max = 16)
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Email(message = "이메일 형식이 아닙니다.")
    @NotBlank
    private String email;

    private String address;
}
