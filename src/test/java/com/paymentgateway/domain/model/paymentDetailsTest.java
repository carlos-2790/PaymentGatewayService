package com.paymentgateway.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentDetails Tests")
class PaymentDetailsTest {

    @Nested
    @DisplayName("CreditCardDetails Tests")
    class CreditCardDetailsTests {

        private String cardNumber;
        private String expiryMonth;
        private String expiryYear;
        private String cvv;
        private String cardHolderName;

        @BeforeEach
        void setUp() {
            cardNumber = "1234567890123456";
            expiryMonth = "12";
            expiryYear = "2025";
            cvv = "123";
            cardHolderName = "John Doe";
        }

        @Test
        @DisplayName("Should create CreditCardDetails successfully with valid data")
        void shouldCreateCreditCardDetailsSuccessfully() {
            CreditCardDetails details = new CreditCardDetails(
                cardNumber,
                expiryMonth,
                expiryYear,
                cvv,
                cardHolderName
            );

            assertThat(details.cardNumber()).isEqualTo(cardNumber);
            assertThat(details.expiryMonth()).isEqualTo(expiryMonth);
            assertThat(details.expiryYear()).isEqualTo(expiryYear);
            assertThat(details.cvv()).isEqualTo(cvv);
            assertThat(details.cardHolderName()).isEqualTo(cardHolderName);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null card number")
        void shouldThrowExceptionForNullCardNumber() {
            assertThatThrownBy(() -> new CreditCardDetails(null, expiryMonth, expiryYear, cvv, cardHolderName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Card number cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for empty card number")
        void shouldThrowExceptionForEmptyCardNumber() {
            assertThatThrownBy(() -> new CreditCardDetails("  ", expiryMonth, expiryYear, cvv, cardHolderName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Card number cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null expiry month")
        void shouldThrowExceptionForNullExpiryMonth() {
            assertThatThrownBy(() -> new CreditCardDetails(cardNumber, null, expiryYear, cvv, cardHolderName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expiry month cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for empty expiry month")
        void shouldThrowExceptionForEmptyExpiryMonth() {
            assertThatThrownBy(() -> new CreditCardDetails(cardNumber, "  ", expiryYear, cvv, cardHolderName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expiry month cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null expiry year")
        void shouldThrowExceptionForNullExpiryYear() {
            assertThatThrownBy(() -> new CreditCardDetails(cardNumber, expiryMonth, null, cvv, cardHolderName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expiry year cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for empty expiry year")
        void shouldThrowExceptionForEmptyExpiryYear() {
            assertThatThrownBy(() -> new CreditCardDetails(cardNumber, expiryMonth, "  ", cvv, cardHolderName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expiry year cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null CVV")
        void shouldThrowExceptionForNullCvv() {
            assertThatThrownBy(() -> new CreditCardDetails(cardNumber, expiryMonth, expiryYear, null, cardHolderName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CVV cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for empty CVV")
        void shouldThrowExceptionForEmptyCvv() {
            assertThatThrownBy(() -> new CreditCardDetails(cardNumber, expiryMonth, expiryYear, "  ", cardHolderName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CVV cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null card holder name")
        void shouldThrowExceptionForNullCardHolderName() {
            assertThatThrownBy(() -> new CreditCardDetails(cardNumber, expiryMonth, expiryYear, cvv, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Card holder name cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for empty card holder name")
        void shouldThrowExceptionForEmptyCardHolderName() {
            assertThatThrownBy(() -> new CreditCardDetails(cardNumber, expiryMonth, expiryYear, cvv, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Card holder name cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("PayPalDetails Tests")
    class PayPalDetailsTests {

        private String email;
        private String returnUrl;
        private String cancelUrl;

        @BeforeEach
        void setUp() {
            email = "test@example.com";
            returnUrl = "http://example.com/return";
            cancelUrl = "http://example.com/cancel";
        }

        @Test
        @DisplayName("Should create PayPalDetails successfully with valid data")
        void shouldCreatePayPalDetailsSuccessfully() {
            PayPalDetails details = new PayPalDetails(
                email,
                returnUrl,
                cancelUrl
            );

            assertThat(details.email()).isEqualTo(email);
            assertThat(details.returnUrl()).isEqualTo(returnUrl);
            assertThat(details.cancelUrl()).isEqualTo(cancelUrl);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null email")
        void shouldThrowExceptionForNullEmail() {
            assertThatThrownBy(() -> new PayPalDetails(null, returnUrl, cancelUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for empty email")
        void shouldThrowExceptionForEmptyEmail() {
            assertThatThrownBy(() -> new PayPalDetails("  ", returnUrl, cancelUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null return URL")
        void shouldThrowExceptionForNullReturnUrl() {
            assertThatThrownBy(() -> new PayPalDetails(email, null, cancelUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Return URL cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for empty return URL")
        void shouldThrowExceptionForEmptyReturnUrl() {
            assertThatThrownBy(() -> new PayPalDetails(email, "  ", cancelUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Return URL cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null cancel URL")
        void shouldThrowExceptionForNullCancelUrl() {
            assertThatThrownBy(() -> new PayPalDetails(email, returnUrl, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cancel URL cannot be null or empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for empty cancel URL")
        void shouldThrowExceptionForEmptyCancelUrl() {
            assertThatThrownBy(() -> new PayPalDetails(email, returnUrl, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cancel URL cannot be null or empty");
        }
    }
}