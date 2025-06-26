package com.paymentgateway.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Detalles de tarjeta de crédito para pagos
 */
@Schema(description = "Detalles de la tarjeta de crédito")
public record CreditCardDetails(
    @Schema(description = "Número de la tarjeta de crédito", example = "4242424242424242", required = true)
    String cardNumber,

    @Schema(description = "Mes de vencimiento (MM)", example = "12", required = true) String expiryMonth,

    @Schema(description = "Año de vencimiento (YYYY)", example = "2028", required = true) String expiryYear,

    @Schema(description = "Código de verificación (CVV)", example = "123", required = true) String cvv,

    @Schema(description = "Nombre del titular de la tarjeta", example = "Juan Perez", required = true)
    String cardHolderName
)
    implements PaymentDetails {
    public CreditCardDetails {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Card number cannot be null or empty");
        }
        if (expiryMonth == null || expiryMonth.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry month cannot be null or empty");
        }
        if (expiryYear == null || expiryYear.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry year cannot be null or empty");
        }
        if (cvv == null || cvv.trim().isEmpty()) {
            throw new IllegalArgumentException("CVV cannot be null or empty");
        }
        if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Card holder name cannot be null or empty");
        }
    }
}
