package com.paymentgateway.application.port.in;

import com.paymentgateway.domain.model.Payment;
import com.paymentgateway.domain.model.PaymentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * port de entrada para consultas de pagos
 */

public interface QueryPaymentUseCase {
    /**
     * obtiene un pago por su id
     */

    Optional<Payment> getPaymentById(UUID paymentId);

    /**
     * Obtiene un pago por referencia
     */
    Optional<Payment> getPaymentByReference(String reference);

    /**
     * Obtiene pagos por clientes
     */

    List<Payment> getPaymentByCustomer(String customerId);

    /**
     * Obtiene pagos por merchant
     */

    List<Payment> getPaymentsByMerchant(String merchantId);

    /**
     * obtiene una lista de pagos por su estado
     */
    List<Payment> getPaymentsByStatus(PaymentStatus status);
}
