package com.paymentgateway.infrastructure.adapter.gateway.stripe;

import java.math.BigDecimal;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.paymentgateway.domain.model.PaymentDetails;
import com.paymentgateway.domain.model.PaymentMethod;
import com.paymentgateway.domain.model.PaymentRequest;
import com.paymentgateway.domain.model.PaymentResponse;
import com.paymentgateway.infrastructure.adapter.gateway.PaymentGatewayStrategy;
import com.paymentgateway.shared.exception.PaymentException;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;

/**
 * Implementacion del Stripe para el patron STRATEGY
 */

@Service("stripeGateway")
public class StripePaymentGateway extends PaymentGatewayStrategy {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);

    private final String apiKey;
    private final BigDecimal minAmount = new BigDecimal("0.50"); // $0.50
    private final BigDecimal maxAmount = new BigDecimal("999999.99"); // $999,999.99
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "USD",
        "EUR",
        "GBP",
        "JPY",
        "CAD",
        "AUD",
        "CHF",
        "SEK",
        "NOK",
        "DKK"
    );

    private static final Set<PaymentMethod> SUPPORTED_PAYMENT_METHODS = Set.of(
        PaymentMethod.CREDIT_CARD,
        PaymentMethod.DEBIT_CARD,
        PaymentMethod.APPLE_PAY,
        PaymentMethod.GOOGLE_PAY,
        PaymentMethod.PAYPAL
    );

    public StripePaymentGateway(@Value("${stripe.api.secret-key}") String apiKey) {
        this.apiKey = apiKey;
        Stripe.apiKey = this.apiKey;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            validatePaymentRequest(request);
            PaymentIntentCreateParams params = buildPaymentIntentParams(request);
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return mapStripeResponseToPaymentResponse(paymentIntent, request.paymentReference());
        } catch (Exception e) {
            log.error("Stripe payment processing failed for reference:{}", request.paymentReference(), e);
            return handleGatewayError(e, request.paymentReference());
        }
    }

    @Override
    public PaymentResponse checkPaymentStatus(String gatewayTransactionId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(gatewayTransactionId);
            return mapStripeResponseToPaymentResponse(paymentIntent, null);
        } catch (Exception e) {
            log.error("Failed to check payment status for transaction: {}", gatewayTransactionId, e);
            throw new PaymentException("Failed to retrieve payment status", e);
        }
    }

    @Override
    public PaymentResponse cancelPayment(String gatewayTransactionId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(gatewayTransactionId);
            PaymentIntent cancelledIntent = paymentIntent.cancel();

            return mapStripeResponseToPaymentResponse(cancelledIntent, null);
        } catch (Exception e) {
            log.error("Failed to cancel payment for transaction: {}", gatewayTransactionId, e);
            return PaymentResponse.failure(null, "Failed to cancel payment", "CANCEL_FAILED");
        }
    }

    @Override
    public PaymentResponse refundPayment(String gatewayTransactionId, String reason) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(gatewayTransactionId)
                .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                .putMetadata("reason", reason)
                .build();
            com.stripe.model.Refund refund = com.stripe.model.Refund.create(params);
            return PaymentResponse.success(
                refund.getId(),
                null,
                new BigDecimal(refund.getAmount()).divide(new BigDecimal(100)),
                refund.getCurrency().toUpperCase(),
                new GatewaySpecificData("stripe", refund.toJson(), null, "Refund processed")
            );
        } catch (Exception e) {
            log.error("Failed to process refund for payment: {}", gatewayTransactionId, e);
            return PaymentResponse.failure(null, "Refund processing failed", "REFUND_FAILED");
        }
    }

    @Override
    public boolean supportsPaymentMethod(PaymentMethod paymentMethod) {
        return SUPPORTED_PAYMENT_METHODS.contains(paymentMethod);
    }

    @Override
    protected String getGatewayProvider() {
        return "STRIPE";
    }

    @Override
    protected void validatePaymentDetails(PaymentDetails paymentDetails) {
        switch (paymentDetails) {
            case CreditCardDetails cardDetails -> validateCreditCardDetails(cardDetails);
            case DigitalWalletDetails walletDetails -> validateWalletDetails(walletDetails);
            default -> throw new IllegalArgumentException("Unsupported payment details type for Stripe");
        }
    }

    @Override
    protected BigDecimal getMaximumAmount() {
        return maxAmount;
    }

    @Override
    protected BigDecimal getMinimumAmount() {
        return minAmount;
    }

    @Override
    protected Set<String> getSupportedCurrencies() {
        return SUPPORTED_CURRENCIES;
    }

    @Override
    protected String determineErrorCode(Exception e) {
        if (e instanceof com.stripe.exception.CardException) {
            return "CARD_ERROR";
        } else if (e instanceof com.stripe.exception.InvalidRequestException) {
            return "INVALID_REQUEST";
        } else if (e instanceof com.stripe.exception.AuthenticationException) {
            return "AUTHENTICATION_ERROR";
        } else if (e instanceof com.stripe.exception.ApiConnectionException) {
            return "CONNECTION_ERROR";
        } else {
            return "UNKNOWN_ERROR";
        }
    }
}