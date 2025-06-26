package com.paymentgateway.infrastructure.adapter.gateway;

import com.paymentgateway.application.port.PaymentGatewayPort;
import com.paymentgateway.domain.model.PaymentDetails;
import com.paymentgateway.domain.model.PaymentRequest;
import com.paymentgateway.domain.model.PaymentResponse;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Implementacion base del Strategy Pattern para pasarelas de pago
 */

@Component
public abstract class PaymentGatewayStrategy implements PaymentGatewayPort {

    /***
     * Valida la solicitud de pago antes de procesarla
     */

    protected void validatePaymentRequest(PaymentRequest request) {
        if (!supportsPaymentMethod(request.paymentMethod())) {
            throw new UnsupportedOperationException(
                String.format("Payment method %s is not supported by %s", request.paymentMethod(), gatewayProvider())
            );
        }

        validateAmount(request.amount());
        validateCurrency(request.currency());
        validatePaymentDetails(request.paymentDetails());
    }

    /**
     * valida el monto del pago
     */
    protected void validateAmount(BigDecimal amount) {
        if (amount.compareTo(getMinimumAmount()) < 0) {
            throw new IllegalArgumentException(
                String.format("Amount %s is below minimum %s for %s", amount, getMinimumAmount(), gatewayProvider())
            );
        }
        if (amount.compareTo(getMaximumAmount()) > 0) {
            throw new IllegalArgumentException(
                String.format("Amount %s exceeds maximum %s for %s", amount, getMaximumAmount(), gatewayProvider())
            );
        }
    }

    /**
     * valida la moneda del pago
     */
    protected void validateCurrency(String currency) {
        if (!getSupportedCurrencies().contains(currency.toUpperCase())) {
            throw new IllegalArgumentException(
                String.format(" Currency %s is not supported by %s", currency, gatewayProvider())
            );
        }
    }

    /**
     * valida los detalles especificos del metodo de pago
     */
    protected abstract void validatePaymentDetails(PaymentDetails paymentDetails);

    /**
     * obtiene el monto maximo permitido
     */
    protected abstract BigDecimal getMaximumAmount();

    /**
     * obtiene el monto minimo permitido
     */
    protected abstract BigDecimal getMinimumAmount();

    /**
     * obtiene las monedas permitidas
     */
    protected abstract Set<String> getSupportedCurrencies();

    /**
     * maneja errores especificos de la pasarela de pago
     */
    protected PaymentResponse handleGatewayError(Exception e, String paymentReference) {
        String errorMessage = "Payment processing failed: " + e.getMessage();
        String errorCode = determineErrorCode(e);
        return PaymentResponse.failure(paymentReference, errorMessage, errorCode);
    }

    /**
     * determina el codigo de error especifico de la pasarela de pago
     */
    protected abstract String determineErrorCode(Exception e);

    /**
     * genera una referencia unica para la transaccion
     */
    protected String generateTransactionReference() {
        return (
            gatewayProvider().toLowerCase() +
            "_" +
            System.currentTimeMillis() +
            "_" +
            java.util.UUID.randomUUID().toString().substring(0, 8)
        );
    }
}
