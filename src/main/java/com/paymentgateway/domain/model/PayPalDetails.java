package com.paymentgateway.domain.model;

/**
 * Detalles de PayPal para pagos
 */
public record PayPalDetails(String email, String returnUrl, String cancelUrl) implements PaymentDetails {
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