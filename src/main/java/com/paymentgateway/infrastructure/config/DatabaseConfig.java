package com.paymentgateway.infrastructure.config;

import java.util.Optional;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

/**
 * Configuración completa de base de datos y JPA para Payment Gateway Service
 * 
 * Esta clase centraliza toda la configuración relacionada con:
 * - Pool de conexiones (HikariCP)
 * - Repositorios JPA y escaneo de entidades
 * - Gestión de transacciones
 * - Auditoria de entidades
 * - Propiedades específicas de Hibernate
 * 
 * @author Payment Gateway Team
 * @version 1.0
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.paymentgateway.infrastructure.persistence",
    transactionManagerRef = "transactionManager"
)
@EntityScan(basePackages = "com.paymentgateway.infrastructure.persistence")
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${spring.datasource.driver-class-name}")
    private String databaseDriverClassName;

    /**
     * Configuración del pool de conexiones HikariCP
     * Optimizado para aplicaciones de pagos con alta concurrencia
     */
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setUsername(databaseUsername);
        config.setPassword(databasePassword);
        config.setDriverClassName(databaseDriverClassName);

        // Configuraciones específicas para pagos
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000); // 30 segundos
        config.setIdleTimeout(600000); // 10 minutos
        config.setMaxLifetime(1800000); // 30 minutos
        config.setLeakDetectionThreshold(60000); // 1 minuto

        // Configuraciones de rendimiento
        config.setPoolName("PaymentGatewayPool");
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);

        return config;
    }

    /**
     * DataSource principal usando HikariCP
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        return new HikariDataSource(hikariConfig());
    }

    /**
     * Configuración del EntityManagerFactory con propiedades optimizadas
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.paymentgateway.infrastructure.persistence");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(false); // Controlado por logging
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(hibernateProperties());

        return em;
    }

    /**
     * Propiedades específicas de Hibernate optimizadas para pagos
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();

        // Configuración de dialecto
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        // Configuración de esquema
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.default_schema", "public");

        // Optimizaciones de rendimiento
        properties.setProperty("hibernate.jdbc.batch_size", "25");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");

        // Cache de segundo nivel (opcional, para producción)
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");

        // Configuración de conexiones
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        properties.setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");

        // Configuración de logging (solo para desarrollo)
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.use_sql_comments", "true");

        // Configuración de timezone
        properties.setProperty("hibernate.jdbc.time_zone", "UTC");

        return properties;
    }

    /**
     * Gestor de transacciones JPA
     * Configurado para manejar transacciones complejas de pagos
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        
        // Configuraciones específicas para pagos
        transactionManager.setDefaultTimeout(30); // 30 segundos timeout
        transactionManager.setRollbackOnCommitFailure(true);
        
        return transactionManager;
    }

    /**
     * Proveedor de auditoría para tracking de cambios
     * Útil para auditar operaciones de pago
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // En un escenario real, obtendríamos el usuario del contexto de seguridad
            // SecurityContextHolder.getContext().getAuthentication().getName()
            return Optional.of("PAYMENT_SYSTEM");
        };
    }

    /**
     * Configuración adicional para validación de conexiones
     * Importante para aplicaciones críticas como pagos
     */
    @Bean
    public DatabaseHealthIndicator databaseHealthIndicator() {
        return new DatabaseHealthIndicator(dataSource());
    }
}

/**
 * Indicador de salud personalizado para la base de datos
 */
class DatabaseHealthIndicator {
    private final DataSource dataSource;
    
    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public boolean isHealthy() {
        try (var connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5 segundos timeout
        } catch (Exception e) {
            return false;
        }
    }
}