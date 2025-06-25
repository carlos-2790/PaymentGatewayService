package com.paymentgateway.infrastructure.adapter.gateway.paypal;

import java.math.BigDecimal;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.paymentgateway.domain.model.CreditCardDetails;
import com.paymentgateway.domain.model.GatewaySpecificData;
import com.paymentgateway.domain.model.PayPalDetails;
import com.paymentgateway.domain.model.PaymentDetails;
import com.paymentgateway.domain.model.PaymentMethod;
import com.paymentgateway.domain.model.PaymentRequest;
import com.paymentgateway.domain.model.PaymentResponse;
import com.paymentgateway.domain.model.PaymentStatus;
import com.paymentgateway.infrastructure.adapter.gateway.PaymentGatewayStrategy;
import com.paymentgateway.shared.exception.PaymentException;

/**
 * Implementación de PayPal para el patrón Strategy
 */
@Service("paypalGateway")
public class PayPalPaymentGateway extends PaymentGatewayStrategy {

    private static final Logger log = LoggerFactory.getLogger(PayPalPaymentGateway.class);
    private final String clientId;
    private final String clientSecret;
    private final String environment;
    private final BigDecimal minAmount = new BigDecimal("1.00"); // $1.00
    private final BigDecimal maxAmount = new BigDecimal("10000.00"); // $10,000.00

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "GBP", "CAD", "AUD", "JPY");
    private static final Set<PaymentMethod> SUPPORTED_PAYMENT_METHODS = Set.of(
            PaymentMethod.PAYPAL,
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.DEBIT_CARD);

    public PayPalPaymentGateway(
            @Value("${paypal.client.id}") String clientId,
            @Value("${paypal.client.secret}") String clientSecret,
            @Value("${paypal.environment:sandbox}") String environment) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.environment = environment;
    }

    @Override
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            validatePaymentRequest(request);

            // Simulación de integración con PayPal SDK
            // En producción aquí usarías el PayPal Checkout SDK
            String transactionId = simulatePayPalPayment(request);

            GatewaySpecificData gatewayData = new GatewaySpecificData(
                    "paypal",
                    "{\"status\":\"COMPLETED\"}",
                    calculatePayPalFees(request.amount()),
                    "PayPal payment processed successfully");
            return PaymentResponse.success(
                    transactionId,
                    request.paymentReference(),
                    request.amount(),
                    request.currency(),
                    gatewayData);

        } catch (Exception e) {
            log.error("PayPal payment processing failed for reference: {}", request.paymentReference(), e);
            return handleGatewayError(e, request.paymentReference());
        }
    }

    @Override
    public PaymentResponse checkPaymentStatus(String gatewayTransactionId) {
        try {
            // Simulación de consulta de estado de pago en PayPal
            PaymentStatus status = simulatePayPalStatusCheck(gatewayTransactionId);

            GatewaySpecificData gatewayData = new GatewaySpecificData(
                    "paypal",
                    "{\"status\": \"" + status + "\"}",
                    null,
                    "Status retrieved from PayPal");
            return new PaymentResponse(
                    status == PaymentStatus.COMPLETED,
                    gatewayTransactionId,
                    null,
                    null,
                    null,
                    status,
                    "Payment status retrieved",
                    null,
                    java.time.LocalDateTime.now(),
                    gatewayData);

        } catch (Exception e) {
            log.error("Failed to check PayPal payment status for transaction: {}", gatewayTransactionId, e);
            throw new PaymentException("Failed to retrieve PayPal payment status", e);
        }
    }

    @Override
    public PaymentResponse cancelPayment(String gatewayTransactionId) {
        try {
            // Simulación de cancelación de pago en PayPal
            boolean canceled = simulatePayPalPaymentCancellation(gatewayTransactionId);

            if (canceled) {
                return new PaymentResponse(
                        true,
                        gatewayTransactionId,
                        null,
                        null,
                        null,
                        PaymentStatus.CANCELLED,
                        "Payment cancelled successfully",
                        null,
                        java.time.LocalDateTime.now(),
                        null);
            } else {
                return PaymentResponse.failure(null, "Failed to cancel PayPal payment", "CANCEL_FAILED");
            }

        } catch (Exception e) {
            log.error("Failed to cancel PayPal payment: {}", gatewayTransactionId, e);
            return PaymentResponse.failure(null, "PayPal cancellation failed", "CANCEL_ERROR");
        }
    }

    @Override
    public PaymentResponse refundPayment(String gatewayTransactionId, String reason) {
        try {
            // Simulación de reembolso en PayPal
            String refundId = simulatePayPalRefund(gatewayTransactionId, reason);
            GatewaySpecificData gatewayData = new GatewaySpecificData(
                    "paypal",
                    "{\"refund_id\": \"" + refundId + "\"}",
                    null,
                    "PayPal refund processed successfully: " + reason);
            return PaymentResponse.success(refundId, null, null, null, gatewayData);
        } catch (Exception e) {
            log.error("Failed to refund PayPal payment: {}", gatewayTransactionId, e);
            return PaymentResponse.failure(null, "PayPal refund failed", "REFUND_FAILED");
        }
    }

    @Override
    public boolean supportsPaymentMethod(PaymentMethod paymentMethod) {
        return SUPPORTED_PAYMENT_METHODS.contains(paymentMethod);
    }

    @Override
    public String gatewayProvider() {
        return "PAYPAL";
    }

    @Override
    protected void validatePaymentDetails(PaymentDetails paymentDetails) {
        switch (paymentDetails) {
            case PayPalDetails paypalDetails -> validatePayPalDetails(paypalDetails);
            case CreditCardDetails creditCardDetails -> validateCreditCardDetails(creditCardDetails);
            default -> throw new IllegalArgumentException("Unsupported payment details type for PayPal");
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
        // Mapeo de errores específicos de PayPal
        String message = e.getMessage().toLowerCase();

        if (message.contains("insufficient funds")) {
            return "INSUFFICIENT_FUNDS";
        } else if (message.contains("invalid account")) {
            return "INVALID_ACCOUNT";
        } else if (message.contains("authentication")) {
            return "AUTHENTICATION_ERROR";
        } else if (message.contains("connection")) {
            return "CONNECTION_ERROR";
        } else {
            return "PAYPAL_ERROR";
        }
    }

    // Métodos de simulación (en producción se usa SDK real de PayPal)
    private String simulatePayPalPayment(PaymentRequest request) {
        // Simulación simple - en producción se usa checkout SDK PayPal
        return "PAYPAL_TXN_" + System.currentTimeMillis();
    }

    private PaymentStatus simulatePayPalStatusCheck(String transactionId) {
        // Simulación - en producción consultaría a la API de PayPal
        return PaymentStatus.COMPLETED;
    }

    private boolean simulatePayPalPaymentCancellation(String transactionId) {
        // Simulación - en producción se usaría la API de PayPal
        return true;
    }

    private String simulatePayPalRefund(String transactionId, String reason) {
        // Simulación - en producción se usaría la API de PayPal
        return "PAYPAL_REFUND_" + System.currentTimeMillis();
    }

    private String calculatePayPalFees(BigDecimal amount) {
        // PayPal cobra 2.9% + $0.30 por transacción - cálculo de comisión de PayPal
        BigDecimal feePercentage = new BigDecimal("0.029"); // 2.9%
        BigDecimal fixedFee = new BigDecimal("0.30"); // $0.30
        BigDecimal totalFee = amount.multiply(feePercentage).add(fixedFee);
        return totalFee.toString();
    }

    private void validatePayPalDetails(PayPalDetails paypalDetails) {
        if (paypalDetails.email() == null || !paypalDetails.email().contains("@")) {
            throw new IllegalArgumentException("PayPal email is required");
        }
        if (paypalDetails.returnUrl() == null || paypalDetails.returnUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("PayPal return URL is required");
        }
        if (paypalDetails.cancelUrl() == null || paypalDetails.cancelUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("PayPal cancel URL is required");
        }
    }

    private void validateCreditCardDetails(CreditCardDetails creditCardDetails) {
        // Validación de detalles de tarjeta de crédito
        if (creditCardDetails.cardNumber() == null || creditCardDetails.cardNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Credit card number is required");
        }
        if (creditCardDetails.cvv() == null || creditCardDetails.cvv().trim().isEmpty()) {
            throw new IllegalArgumentException("CVV is required");
        }
    }
}
