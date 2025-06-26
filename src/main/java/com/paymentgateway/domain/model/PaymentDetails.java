package com.paymentgateway.domain.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Detalles especificos del pago
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = CreditCardDetails.class, name = "CREDIT_CARD"),
        @JsonSubTypes.Type(value = PayPalDetails.class, name = "PAYPAL")
    }
)
public sealed interface PaymentDetails permits CreditCardDetails, PayPalDetails {}
