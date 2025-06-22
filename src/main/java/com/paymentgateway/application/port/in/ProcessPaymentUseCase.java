package com.paymentgateway.application.port.in;

import com.paymentgateway.domain.model.Payment;
import com.paymentgateway.domain.model.PaymentRequest;

/**
 * Puerto de entrada del dominio para el caso de uso de procesamiento de pago
 */

public interface ProcessPaymentUseCase {
    /**
     * procesa un nuevo pago
     */
    Payment processPayment(PaymentRequest request);
}
