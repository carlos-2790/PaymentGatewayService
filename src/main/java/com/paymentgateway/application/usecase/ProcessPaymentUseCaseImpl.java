package com.paymentgateway.application.usecase;

import org.springframework.stereotype.Service;

import com.paymentgateway.application.port.in.ProcessPaymentUseCase;
import com.paymentgateway.domain.model.Payment;
import com.paymentgateway.domain.model.PaymentRequest;

@Service
public class ProcessPaymentUseCaseImpl implements ProcessPaymentUseCase {

    @Override
    public Payment processPayment(PaymentRequest request) {
        // Lógica de negocio para procesar el pago
        // Crear la entidad de pago (estado inicial: PENDING)
        Payment payment = new Payment(
            request.paymentReference(),
            request.amount(),
            request.currency(),
            request.paymentMethod(),
            "stripe", // Gateway de ejemplo
            request.customerId(),
            request.merchantId(),
            request.description()
        );
        
        // Seguir el flujo correcto de estados: PENDING -> PROCESSING -> COMPLETED
        payment.markAsProcessing(); // Cambiar a PROCESSING
        payment.markAsCompleted("txn_123456789"); // Luego completar
        
        return payment;
    }
}
