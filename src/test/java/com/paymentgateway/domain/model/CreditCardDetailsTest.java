package com.paymentgateway.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.time.YearMonth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CreditCardDetailsTest {

    @Test
    @DisplayName(" ✅ Test credit card details - Valid credit card details")
    public void testCreditCardDetails() {
        CreditCardDetails creditCardDetails = new CreditCardDetails("1234567890123456", "12", "2028", "123",
                "Juan Perez");
        assertThat(creditCardDetails.cardNumber()).isEqualTo("1234567890123456");
        assertThat(creditCardDetails.expiryMonth()).isEqualTo("12");
        assertThat(creditCardDetails.expiryYear()).isEqualTo("2028");
        assertThat(creditCardDetails.cvv()).isEqualTo("123");
        assertThat(creditCardDetails.cardHolderName()).isEqualTo("Juan Perez");
    }

    @Test
    @DisplayName(" ❌ Test credit card details - Card number cannot be null or empty")
    public void testInvalidCardNumberCreditCardDetails() {
        assertThatThrownBy(() -> new CreditCardDetails("", "12", "2028", "123", "Juan Perez"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Card number cannot be null or empty");
    }

    @Test
    @DisplayName("❌ Test Credit Card Details - Invalid Expiry Month")
    public void testInvalidExpiryMonthCreditCardDetails() {
        YearMonth expiredDate = YearMonth.now().minusYears(1);
        String expiredMonth = String.format("%02d", expiredDate.getMonthValue());
        String expiredYear = String.format("%04d", expiredDate.getYear());

        assertThatThrownBy(() -> new CreditCardDetails("4242424242424242", expiredMonth, expiredYear, "123", "Juan Perez"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("La tarjeta ha expirado");
    }

}
