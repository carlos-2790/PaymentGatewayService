package com.paymentgateway.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PaymentRequestTest {

    @Test
    @DisplayName("✅ Test payment request - Valid payment request")
    public void testValidPaymentRequest() {
        PaymentDetails paymentDetails = new CreditCardDetails("1234567890123456", "09", "2027", "123", "Juan Perez");

        PaymentRequest paymentRequest = new PaymentRequest(
                "1234567890",
                new BigDecimal("100"),
                "USD",
                PaymentMethod.CREDIT_CARD,
                "1234567890",
                "1234567890",
                "Test payment",
                paymentDetails);

        assertThat(paymentRequest.paymentReference()).isEqualTo("1234567890");
        assertThat(paymentRequest.amount()).isEqualTo(new BigDecimal("100"));
        assertThat(paymentRequest.currency()).isEqualTo("USD");
        assertThat(paymentRequest.paymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(paymentRequest.customerId()).isEqualTo("1234567890");
        assertThat(paymentRequest.merchantId()).isEqualTo("1234567890");
        assertThat(paymentRequest.description()).isEqualTo("Test payment");
        assertThat(paymentRequest.paymentDetails()).isNotNull();
    }

    @Test
    @DisplayName("❌ Test payment request - Negative Amount")
    public void testNegativeAmountPaymentRequest() {
        assertThatThrownBy(() -> new PaymentRequest(
                "1234567890",
                new BigDecimal("-100"),
                "USD",
                PaymentMethod.CREDIT_CARD,
                "1234567890",
                "1234567890",
                "Test payment",
                new CreditCardDetails("1234567890123456", "09", "2027", "123", "Juan Perez")))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Amount must be positive");
    }
}
