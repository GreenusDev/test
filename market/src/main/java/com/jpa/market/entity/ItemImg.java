package com.jpa.market.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_img")
@Getter
@ToString(exclude = "item") // 중요: 연관관계 필드는 ToString에서 제외
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 빌더는 기본적으로 모든 필드를 받는 생성자가 있어야 값을 넣을 수 있음(빌더랑 세트_)
@AllArgsConstructor
@Builder
public class ItemImg extends BaseEntity {

    @Id
    @Column(name="item_img_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imgName; //이미지 파일명

    private String oriImgName; //원본 이미지 파일명

    private String imgUrl; //이미지 조회 경로

    private String repImgYn; //대표 이미지 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    // 이미지 정보를 업데이트하는 비즈니스 로직 (Setter 대신 사용)
    // 이미지 생성의 책임은 Item이 가지고, 비즈니스 로직을 가지지 않으므로 create는 builder나 대신할 예정
    public void updateItemImg(String oriImgName, String imgName, String imgUrl, String repImgYn) {
        this.oriImgName = oriImgName;
        this.imgName = imgName;
        this.imgUrl = imgUrl;
        this.repImgYn = repImgYn; // 대표 이미지 여부도 함께 변경
    }

    // 만약 파일은 수정 안 하고 '대표 여부'만 바꾸고 싶을 때를 위한 메서드도 추가하면 좋습니다.
    public void updateRepImgYn(String repImgYn) {
        this.repImgYn = repImgYn;
    }
}