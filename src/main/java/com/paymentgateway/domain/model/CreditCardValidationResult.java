package com.paymentgateway.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de la validacion de la tarjeta de credito")
public record CreditCardValidationResult(
        @Schema(description = "Indica si la tarjeta es valida", example = "true") boolean isValid,

        @Schema(description = "Tipo de tarjeta detectado", example = "VISA") String cardType,

        @Schema(description = "Numero de tarjeta enmascarado", example = "****-****-****-4242") String maskedCardNumber,

        @Schema(description = "Mensaje de validacion", example = "Tarjeta valida") String message,

        @Schema(description = "Indica si la tarjeta esta expirada", example = "false") boolean isExpired,

        @Schema(description = "Dias restantes para que expire la tarjeta", example = "365") long daysUntilExpiry) {

    public static CreditCardValidationResult valid(String cardType, String maskedCardNumber, long daysUntilExpiry) {

        return new CreditCardValidationResult(true, cardType, maskedCardNumber, "Tarjeta valida", false,
                daysUntilExpiry);

    }

    public static CreditCardValidationResult invalid(String reason) {
        return new CreditCardValidationResult(false, "UNKNOWN", "****-****-****-****", reason, false, 0);
    }

    public static CreditCardValidationResult expired(String cardType, String maskCardNumber) {
        return new CreditCardValidationResult(false, cardType, maskCardNumber, "Tarjeta expirada", true, 0);
    }

}
