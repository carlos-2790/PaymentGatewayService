package com.paymentgateway.infrastructure.adapter.gateway.stripe;

import com.paymentgateway.domain.model.CreditCardDetails;
import com.paymentgateway.domain.model.GatewaySpecificData;
import com.paymentgateway.domain.model.PaymentDetails;
import com.paymentgateway.domain.model.PaymentMethod;
import com.paymentgateway.domain.model.PaymentRequest;
import com.paymentgateway.domain.model.PaymentResponse;
import com.paymentgateway.domain.model.PaymentStatus;
import com.paymentgateway.infrastructure.adapter.gateway.PaymentGatewayStrategy;
import com.paymentgateway.shared.exception.PaymentException;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import java.math.BigDecimal;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        PaymentMethod.GOOGLE_PAY
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
    public String gatewayProvider() {
        return "STRIPE";
    }

    @Override
    protected void validatePaymentDetails(PaymentDetails paymentDetails) {
        switch (paymentDetails) {
            case CreditCardDetails cardDetails -> validateCreditCardDetails(cardDetails);
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

    // Métodos privados auxiliares
    private PaymentIntentCreateParams buildPaymentIntentParams(PaymentRequest request) {
        return PaymentIntentCreateParams.builder()
            .setAmount(request.amount().multiply(new BigDecimal(100)).longValue()) // Stripe usa centavos
            .setCurrency(request.currency().toLowerCase())
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
            )
            .putMetadata("payment_reference", request.paymentReference())
            .build();
    }

    private PaymentResponse mapStripeResponseToPaymentResponse(PaymentIntent paymentIntent, String paymentReference) {
        PaymentStatus status = mapStripeStatus(paymentIntent.getStatus());
        BigDecimal amount = new BigDecimal(paymentIntent.getAmount()).divide(new BigDecimal(100));

        GatewaySpecificData gatewayData = new GatewaySpecificData(
            "stripe",
            paymentIntent.toJson(),
            calculateStripeFees(amount).toString(),
            "Stripe payment: " + paymentIntent.getStatus()
        );

        if (status == PaymentStatus.COMPLETED) {
            return PaymentResponse.success(
                paymentIntent.getId(),
                paymentReference,
                amount,
                paymentIntent.getCurrency().toUpperCase(),
                gatewayData
            );
        } else {
            return PaymentResponse.failure(
                paymentReference,
                "Payment " + paymentIntent.getStatus(),
                determineErrorCodeFromStatus(paymentIntent.getStatus())
            );
        }
    }

    private PaymentStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "succeeded" -> PaymentStatus.COMPLETED;
            case "processing" -> PaymentStatus.PROCESSING;
            case "canceled" -> PaymentStatus.CANCELLED;
            case "requires_payment_method", "requires_confirmation", "requires_action" -> PaymentStatus.PENDING;
            default -> PaymentStatus.FAILED;
        };
    }

    private String determineErrorCodeFromStatus(String status) {
        return switch (status) {
            case "canceled" -> "PAYMENT_CANCELED";
            case "requires_payment_method" -> "INVALID_PAYMENT_METHOD";
            case "requires_action" -> "ACTION_REQUIRED";
            default -> "PAYMENT_FAILED";
        };
    }

    private BigDecimal calculateStripeFees(BigDecimal amount) {
        // Stripe cobra 2.9% + $0.30 por transacción
        BigDecimal feePercentage = new BigDecimal("0.029");
        BigDecimal fixedFee = new BigDecimal("0.30");
        return amount.multiply(feePercentage).add(fixedFee);
    }

    private void validateCreditCardDetails(CreditCardDetails cardDetails) {
        if (cardDetails.cardNumber() == null || cardDetails.cardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Credit card number is required");
        }
        if (cardDetails.cvv() == null || cardDetails.cvv().trim().isEmpty()) {
            throw new IllegalArgumentException("CVV is required");
        }
        if (cardDetails.expiryMonth() == null || cardDetails.expiryYear() == null) {
            throw new IllegalArgumentException("Expiry date is required");
        }
    }
}
