package com.paymentgateway.domain.model;

/**
 * Detalles especificos del pago
 */
public sealed interface PaymentDetails permits CreditCardDetails, PayPalDetails {
}
