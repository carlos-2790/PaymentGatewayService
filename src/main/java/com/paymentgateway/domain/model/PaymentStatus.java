package com.paymentgateway.domain.model;

/**
 * Estados del pago en el dominio
 */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED
}
