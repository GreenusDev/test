package com.jpa.market.entity;

import com.jpa.market.constant.OAuthType;
import com.jpa.market.constant.Role;
import com.jpa.market.dto.MemberJoinDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "member")
@Getter
@ToString //테스트 위해서 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Id
    @Column(name="member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loginId;   // 로그인용 ID

    private String password;

    private String name;

    @Column(unique = true)
    private String email;

    private String address;

    @Enumerated(EnumType.STRING) // DB에 숫자가 아닌 "KAKAO", "NONE" 문자열로 저장되도록 설정
    private OAuthType oauthType; // 일반 가입자는 NONE, 카카오는 KAKAO 저장

    //자바의 enum타입을 엔티티 속성으로 지정
    //enum을 사용할때 기본적으로 순서가 지정되는데,
    //enum의 순서가 바뀌면 문제가 발생할 수 있으므로 옵션을 이용하여 STring으로 저장할것을 권장
    @Enumerated(EnumType.STRING)
    private Role role;

    //Member엔티티를 생성하는 메서드
    //회원을 생성하는 메소드를 만들어서 관리를 하면
    //코드가 변경되더라도 한 군데만 변경하면 됨
    public static Member createMember(MemberJoinDto memberJoinDto, PasswordEncoder passwordEncoder){
        Member member = new Member();
        member.loginId = memberJoinDto.getLoginId(); // loginId 추가
        member.name = memberJoinDto.getName();
        member.email = memberJoinDto.getEmail();
        member.address = memberJoinDto.getAddress();

        // 암호화된 비밀번호 저장
        member.password = passwordEncoder.encode(memberJoinDto.getPassword());
        member.role = Role.USER;
        member.oauthType = OAuthType.MACA;
        return member;
    }

    //소셜 로그인용 멤버 생성 메서드
    public static Member createOAuthMember(String loginId, String nickname, String email, String password, OAuthType oauthType) {
        Member member = new Member();
        member.loginId = loginId;      // 카카오 고유 번호를 로그인 ID로 사용
        member.name = nickname;
        member.email = email;
        member.password = password; // 더미 비밀번호
        member.role = Role.USER;
        member.oauthType = oauthType; // OAuth 타입 설정

        return member;
    }
}
