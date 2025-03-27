package com.kdu.rizzlers.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.kdu.rizzlers.repository")
@EntityScan(basePackages = "com.kdu.rizzlers.entity")
public class JpaConfig {
    // JPA specific configurations will go here
} 