package com.paymentgateway.application.port;

import com.paymentgateway.domain.model.PaymentMethod;
import com.paymentgateway.domain.model.PaymentRequest;
import com.paymentgateway.domain.model.PaymentResponse;

/**
 * Puerto de salida del dominio para la pasarela de pago
 * Implementa el patron Strategy
 */

public interface PaymentGatewayPort {
    //Metodo para procesar el pago atraves de una tarjeta de credito
    PaymentResponse processPayment(PaymentRequest request);

    /**
     * verifica el estado del pago en la pasarela
     */

    PaymentResponse checkPaymentStatus(String gatewayTransactionId);

    /**
     * cancela un pago pendiente
     */

    PaymentResponse cancelPayment(String gatewayTransactionId);

    /**
     * Procesa un reembolso de pago
     */
    PaymentResponse refundPayment(String gatewayTransactionId, String reason);

    /**
     * Verifica si esta pasarela soporta medio de pago
     */

    boolean supportsPaymentMethod(PaymentMethod paymentMethod);

    /**
     * Obtiene el identificador unico de la pasareela de pago
     */
    String gatewayProvider();
}
