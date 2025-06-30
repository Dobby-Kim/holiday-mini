package com.holidaymini.repository.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class QueryDslConfig {

    private final EntityManager entityManager;

    /**
     * Creates and provides a {@link JPAQueryFactory} bean for building type-safe JPA queries using QueryDSL.
     *
     * @return a {@code JPAQueryFactory} initialized with the application's {@code EntityManager}
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
