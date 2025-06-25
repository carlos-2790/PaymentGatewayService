package com.paymentgateway.domain.model;

/**
 * Detalles de tarjeta de crédito para pagos
 */
public record CreditCardDetails(String cardNumber, String expiryMonth, String expiryYear, String cvv, String cardHolderName)
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