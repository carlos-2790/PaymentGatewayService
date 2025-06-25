package com.paymentgateway.domain.model;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para recibir requests de pago desde el endpoint REST
 * Evita problemas de deserialización con interfaces selladas
 */
@Schema(description = "Datos del pago a procesar")
public record PaymentRequestDTO(
    @Schema(description = "Referencia única del pago", example = "test-ref-123", required = true)
    String paymentReference,
    
    @Schema(description = "Monto del pago", example = "100.50", required = true)
    BigDecimal amount,
    
    @Schema(description = "Código de moneda ISO 4217", example = "USD", required = true)
    String currency,
    
    @Schema(description = "Método de pago", example = "CREDIT_CARD", required = true)
    PaymentMethod paymentMethod,
    
    @Schema(description = "ID del cliente", example = "cust-123", required = true)
    String customerId,
    
    @Schema(description = "ID del comercio", example = "merch-456", required = true)
    String merchantId,
    
    @Schema(description = "Descripción del pago", example = "Test Payment")
    String description,
    
    @Schema(description = "Detalles específicos del método de pago", required = true)
    CreditCardDetails paymentDetails // Usar directamente CreditCardDetails por simplicidad
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