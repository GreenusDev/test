package com.jpa.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

//엔티티 생명주기 감시자
@EntityListeners(value = {AuditingEntityListener.class})
//테이블이 되지 않는 부모 엔티티
@MappedSuperclass
//내가 값을 설정하는게 아니니까 setter필요없음
@Getter
public abstract class BaseTimeEntity {
    @CreatedDate // 엔티티가 생성되어 저장될 때의 시간이 자동으로 저장
    @Column(updatable = false) //데이터베이스에 한 번 저장된 이후에는 수정할 수 없도록 제한
    private LocalDateTime regTime;

    //엔티티가 수정될때마다 시간 변경
    @LastModifiedDate
    private LocalDateTime updateTime;
}
