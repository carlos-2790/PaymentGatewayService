package com.paymentgateway.infrastructure.adapter.gateway;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paymentgateway.application.port.PaymentGatewayPort;
import com.paymentgateway.domain.model.PaymentMethod;
import com.paymentgateway.shared.exception.PaymentException;

/**
 * Factory para seleccionar la pasarela de pago apropiada
 * Implementa el patrón Factory junto con Strategy
 */

@Component
public class PaymentGatewayFactory {

    private final Map<String, PaymentGatewayPort> gateways;

    public PaymentGatewayFactory(
            @Qualifier("paypalGateway") PaymentGatewayPort paypalGateway,
            @Qualifier("stripeGateway") PaymentGatewayPort stripeGateway) {
        this.gateways = Map.of("PAYPAL", paypalGateway, "STRIPE", stripeGateway);
    }

    /**
     * obtiene una pasarela especifica por nombre
     */
    public PaymentGatewayPort getGateway(String gatewayProvider) {
        PaymentGatewayPort gateway = gateways.get(gatewayProvider.toUpperCase());
        if (gateway == null) {
            throw new PaymentException("Unsupported payment gateway: " + gatewayProvider, "UNSUPPORTED_GATEWAY");
        }
        return gateway;
    }

    /**
     * obtiene la mejor pasarela para un metodo de pago especifico
     */
    public PaymentGatewayPort getBestGatewayForPaymentMethod(PaymentMethod paymentMethod) {
        List<PaymentGatewayPort> compatibleGateways = gateways
                .values()
                .stream()
                .filter(gateway -> gateway.supportsPaymentMethod(paymentMethod))
                .collect(Collectors.toList());
        if (compatibleGateways.isEmpty()) {
            throw new PaymentException(
                    "No compatible payment gateway found for payment method: " + paymentMethod,
                    "NO_GATEWAY_AVAILABLE");
        }
        return selectOptimalGateway(compatibleGateways, paymentMethod);
    }

    /**
     * Obtiene todas las pasarelas disponibles
     */
    public Map<String, PaymentGatewayPort> getAllGateways() {
        return Map.copyOf(gateways);
    }

    /**
     * Selecciona la pasarela optima basada en criterios de negocio
     */
    private PaymentGatewayPort selectOptimalGateway(List<PaymentGatewayPort> compatibleGateways,
            PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case DEBIT_CARD, CREDIT_CARD, APPLE_PAY, GOOGLE_PAY -> compatibleGateways.stream()
                    .filter(gateway -> "STRIPE".equals(gateway.gatewayProvider()))
                    .findFirst()
                    .orElse(compatibleGateways.get(0));
            case PAYPAL -> compatibleGateways.stream()
                    .filter(gateway -> "PAYPAL".equals(gateway.gatewayProvider()))
                    .findFirst()
                    .orElse(compatibleGateways.get(0));

            default -> compatibleGateways.get(0);// por defecto se selecciona la primera pasarela disponible
        };
    }
}