package com.paymentgateway.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.paymentgateway.shared.exception.PaymentException;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * Entidad principal del dominio de pagos
 * Implementa Domain Driven Design (DDD)
 */
@Entity
@Table(name = "payments")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entidad de pago procesado")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "ID único del pago", example = "0be0f74c-7710-4a91-a49b-8b225d61d968")
    private UUID id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Referencia única del pago", example = "test-ref-123")
    private String paymentReference;

    @Column(nullable = false)
    @Schema(description = "Monto del pago", example = "100.50")
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    @Schema(description = "Código de moneda ISO 4217", example = "USD")
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Estado actual del pago", example = "COMPLETED")
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Método de pago utilizado", example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;

    @Column(name = "gateway_provider", nullable = false)
    @Schema(description = "Proveedor de la pasarela de pago", example = "stripe")
    private String gatewayProvider;

    @Column(name = "gateway_transaction_id")
    @Schema(description = "ID de transacción de la pasarela", example = "txn_123456789")
    private String gatewayTransactionId;

    @Column(name = "customer_id", nullable = false)
    @Schema(description = "ID del cliente", example = "cust-123")
    private String customerId;

    @Column(name = "merchant_id", nullable = false)
    @Schema(description = "ID del comercio", example = "merch-456")
    private String merchantId;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Descripción del pago", example = "Test Payment")
    private String description;

    @Column(name = "failure_reason")
    @Schema(description = "Razón del fallo en caso de error")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "Fecha y hora de creación", example = "2025-06-25T19:32:48.1317594")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "Fecha y hora de última actualización", example = "2025-06-25T19:32:49.1317594")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    @Schema(description = "Fecha y hora de completado", example = "2025-06-25T19:32:49.1317594")
    private LocalDateTime completedAt;

    @Version
    @Schema(description = "Versión para control de concurrencia", example = "2")
    private Long version;

    // Constructor de dominio
    public Payment(
            String paymentReference,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            String gatewayProvider,
            String customerId,
            String merchantId,
            String description) {
        validatePaymentCreation(amount, currency, paymentMethod, gatewayProvider, customerId, merchantId);

        this.id = UUID.randomUUID();
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.gatewayProvider = gatewayProvider;
        this.customerId = customerId;
        this.merchantId = merchantId;
        this.description = description;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Métodos de negocio del dominio
    public void markAsProcessing() {
        if (this.status != PaymentStatus.PENDING) {
            throw new PaymentException("Payment can only be marked as processing from pending status");
        }
        this.status = PaymentStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsCompleted(String gatewayTransactionId) {
        if (this.status != PaymentStatus.PROCESSING) {
            throw new PaymentException("Payment can only be completed from processing status");
        }
        this.status = PaymentStatus.COMPLETED;
        this.gatewayTransactionId = gatewayTransactionId;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsFailed(String failureReason) {
        if (this.status == PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot fail a completed payment");
        }
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == PaymentStatus.COMPLETED) {
            throw new PaymentException("Cannot cancel a completed payment");
        }
        this.status = PaymentStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    @Schema(description = "Indica si el pago está completado", example = "true")
    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }

    @Schema(description = "Indica si el pago está pendiente", example = "false")
    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    @Schema(description = "Indica si el pago falló", example = "false")
    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    private void validatePaymentCreation(
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            String gatewayProvider,
            String customerId,
            String merchantId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Payment amount must be positive");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new PaymentException("Currency is required");
        }
        if (paymentMethod == null) {
            throw new PaymentException("Payment method is required");
        }
        if (gatewayProvider == null || gatewayProvider.trim().isEmpty()) {
            throw new PaymentException("Gateway provider is required");
        }
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new PaymentException("Customer ID is required");
        }
        if (merchantId == null || merchantId.trim().isEmpty()) {
            throw new PaymentException("Merchant ID is required");
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getGatewayProvider() {
        return gatewayProvider;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getDescription() {
        return description;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Long getVersion() {
        return version;
    }
}
