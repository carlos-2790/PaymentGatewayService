package com.paymentgateway.domain.model;

import com.paymentgateway.shared.exception.PaymentException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad principal del dominio de pagos
 * Implementa Domain Driven Design (DDD)
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String paymentReference;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "gateway_provider", nullable = false)
    private String gatewayProvider;

    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    private Long version;

    // Constructor protegido para JPA
    protected Payment() {}

    // Constructor de dominio
    public Payment(
        String paymentReference,
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        String gatewayProvider,
        String customerId,
        String merchantId,
        String description
    ) {
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

    public boolean isCompleted() {
        return this.status == PaymentStatus.COMPLETED;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    private void validatePaymentCreation(
        BigDecimal amount,
        String currency,
        PaymentMethod paymentMethod,
        String gatewayProvider,
        String customerId,
        String merchantId
    ) {
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
