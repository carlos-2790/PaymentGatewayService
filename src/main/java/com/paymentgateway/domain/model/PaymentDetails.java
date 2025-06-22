package com.paymentgateway.domain.model;

import com.paymentgateway.domain.model.CreditCardDetails.PayPalDetails;

/**
 * Detalles especificos del pago
 */

public sealed interface PaymentDetails permits CreditCardDetails, PayPalDetails {}

record CreditCardDetails(String cardNumber, String expiryMonth, String expiryYear, String cvv, String cardHolderName)
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

    record PayPalDetails(String email, String returnUrl, String cancelUrl) implements PaymentDetails {
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
}
