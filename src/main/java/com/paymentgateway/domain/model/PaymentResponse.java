package com.paymentgateway.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *Respuesta de la pasarela de pago
 */

public record PaymentResponse(
    boolean success,
    String gatewayTransactionId,
    String paymentReference,
    BigDecimal amount,
    String currency,
    PaymentStatus status,
    String message,
    String errorCode,
    LocalDateTime processedAt,
    GatewaySpecificData gatewayData
) {
    public static PaymentResponse success(
        String gatewayTransactionId,
        String paymentReference,
        BigDecimal amount,
        String currency,
        GatewaySpecificData gatewayData
    ) {
        return new PaymentResponse(
            true,
            gatewayTransactionId,
            paymentReference,
            amount,
            currency,
            PaymentStatus.COMPLETED,
            "Payment processed successfully",
            null,
            LocalDateTime.now(),
            gatewayData
        );
    }

    public static PaymentResponse failure(String paymentReference, String message, String errorCode) {
        return new PaymentResponse(
            false,
            null,
            paymentReference,
            null,
            null,
            PaymentStatus.FAILED,
            message,
            errorCode,
            LocalDateTime.now(),
            null
        );
    }
}
