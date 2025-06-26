package com.paymentgateway.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Detalles de PayPal para pagos
 */
@Schema(description = "Detalles del pago con PayPal")
public record PayPalDetails(
    @Schema(description = "Email del usuario de PayPal", example = "user@example.com", required = true) String email,

    @Schema(description = "URL de retorno en caso de éxito", example = "https://merchant.com/success", required = true)
    String returnUrl,

    @Schema(
        description = "URL de retorno en caso de cancelación",
        example = "https://merchant.com/cancel",
        required = true
    )
    String cancelUrl
)
    implements PaymentDetails {
    public PayPalDetails {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (returnUrl == null || returnUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Return URL cannot be null or empty");
        }
        if (cancelUrl == null || cancelUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancel URL cannot be null or empty");
        }
    }
}
