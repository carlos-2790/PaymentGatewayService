package com.paymentgateway.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de base de datos y JPA
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.paymentgateway.infrastructure.persistence")
@EntityScan(basePackages = "com.paymentgateway.infrastructure.persistence")
@EnableTransactionManagement
public class DatabaseConfig {
    // Configuración adicional si es necesaria
} 