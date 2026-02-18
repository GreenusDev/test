package com.jpa.market.config.jpa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//스프링 애플리케이션 컨텍스트에 빈(Bean) 구성을 제공하는 클래스라는 것을 나타내며,
//스프링 IoC 컨테이너에 의해 관리되는 빈으로 등록
@Configuration

//JPA(Auditing) 기능을 활성화
//JPA Auditing은 엔티티의 생성일(createdDate)과
//수정일(lastModifiedDate)을 자동으로 관리하기 위해 사용
@EnableJpaAuditing
public class AuditConfig {

    //AuditorAware 인터페이스를 구현하는 빈을 생성하는 역할
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

}
