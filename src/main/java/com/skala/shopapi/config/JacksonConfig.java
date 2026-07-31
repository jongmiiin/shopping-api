package com.skala.shopapi.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JPA 엔티티를 Response로 직접 반환하는 구조라, 같은 트랜잭션 내에서 지연 로딩 프록시로 먼저 로드된
 * 엔티티가 이후 조회에서 그대로 재사용되면 Hibernate 프록시(ByteBuddy 서브클래스)가 그대로 직렬화되어
 * "hibernateLazyInitializer" 같은 내부 필드가 JSON에 노출될 수 있다. jackson-datatype-hibernate6는
 * Hibernate 7(현재 프로젝트가 사용하는 버전)과 호환되지 않아 쓸 수 없으므로, 해당 내부 필드만 전역으로
 * 무시하도록 Jackson(Boot 4의 Jackson 3 기반 JsonMapper)을 설정한다.
 */
@Configuration
public class JacksonConfig {

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    static class HibernateProxyMixIn {
    }

    @Bean
    public JsonMapperBuilderCustomizer hibernateProxyJacksonCustomizer() {
        return builder -> builder.addMixIn(Object.class, HibernateProxyMixIn.class);
    }
}
