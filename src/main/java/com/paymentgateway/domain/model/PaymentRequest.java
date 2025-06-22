package com.paymentgateway.domain.model;

import java.math.BigDecimal;

/**
 * Entidad principal del dominio de pagos
 * Implementa Domain Driven Design (DDD)
 */

public record PaymentRequest(
    String paymentReference,
    BigDecimal amount,
    String currency,
    PaymentMethod paymentMethod,
    String customerId,
    String merchantId,
    String description,
    PaymentDetails paymentDetails
) {
    public PaymentRequest {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (merchantId == null || merchantId.trim().isEmpty()) {
            throw new IllegalArgumentException("Merchant ID is required");
        }
    }
}
