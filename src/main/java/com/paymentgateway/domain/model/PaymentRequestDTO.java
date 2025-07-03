package com.paymentgateway.domain.model;

import java.math.BigDecimal;

import com.paymentgateway.infrastructure.web.validation.ValidCurrency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para recibir requests de pago desde el endpoint REST
 * Evita problemas de deserialización con interfaces selladas
 */
@Schema(description = "Datos del pago a procesar")
public record PaymentRequestDTO(
    @Schema(description = "Referencia única del pago", example = "test-ref-123", required = true)
    @NotBlank(message = "Payment reference is required")
    String paymentReference,

    @Schema(description = "Monto del pago", example = "100.50", required = true) 
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    BigDecimal amount,

    @Schema(description = "Código de moneda ISO 4217", example = "USD", required = true) 
    @NotBlank(message = "Currency is required")
    @ValidCurrency(message = "Currency not supported")
    String currency,

    @Schema(description = "Método de pago", example = "CREDIT_CARD", required = true) 
    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,

    @Schema(description = "ID del cliente", example = "cust-123", required = true) 
    @NotBlank(message = "Customer ID is required")
    String customerId,

    @Schema(description = "ID del comercio", example = "merch-456", required = true) 
    @NotBlank(message = "Merchant ID is required")
    String merchantId,

    @Schema(description = "Descripción del pago", example = "Test Payment") 
    String description,

    @Schema(description = "Detalles específicos del método de pago", required = true) 
    @NotNull(message = "Payment details are required")
    PaymentDetails paymentDetails
) {
    public PaymentRequestDTO {
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

    /**
     * Convierte el DTO a la entidad de dominio PaymentRequest
     */
    public PaymentRequest toPaymentRequest() {
        return new PaymentRequest(
            paymentReference,
            amount,
            currency,
            paymentMethod,
            customerId,
            merchantId,
            description,
            paymentDetails
        );
    }
}
