package com.paymentgateway.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para la petición de validación de tarjeta de crédito
 */
@Schema(description = "Petición de validación de tarjeta de crédito")
public record CreditCardValidationRequest(
    @Schema(description = "Número de la tarjeta de crédito", example = "4242424242424242", required = true)
    String cardNumber,

    @Schema(description = "Mes de vencimiento (MM)", example = "12", required = true) 
    String expiryMonth,

    @Schema(description = "Año de vencimiento (YYYY)", example = "2028", required = true) 
    String expiryYear,

    @Schema(description = "Código de verificación (CVV)", example = "123", required = true) 
    String cvv,

    @Schema(description = "Nombre del titular de la tarjeta", example = "Juan Perez", required = true)
    String cardHolderName
) {
    // Sin validaciones en el constructor para permitir la deserialización
} 