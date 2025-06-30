package com.paymentgateway.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.paymentgateway.domain.model.PaymentStatus;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {
    
    // Buscar por referencia de pago
    Optional<PaymentEntity> findByPaymentReference(String paymentReference);
    
    // Buscar por ID de merchant
    List<PaymentEntity> findByMerchantId(String merchantId);
    
    // Buscar por ID de cliente
    List<PaymentEntity> findByCustomerId(String customerId);
    
    // Buscar por estado
    List<PaymentEntity> findByStatus(PaymentStatus status);
    
    // Buscar por proveedor de pasarela
    List<PaymentEntity> findByGateWayProvider(String gateWayProvider);
    
    // Buscar por ID de transacción de la pasarela
    Optional<PaymentEntity> findByGatewayTransactionId(String gatewayTransactionId);
    
    // Verificar si existe por referencia de pago
    boolean existsByPaymentReference(String paymentReference);
    
    // Contar por estado
    long countByStatus(PaymentStatus status);
}
