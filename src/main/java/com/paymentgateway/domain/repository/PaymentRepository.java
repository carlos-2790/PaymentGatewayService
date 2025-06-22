package com.paymentgateway.domain.repository;

import com.paymentgateway.domain.model.Payment;
import com.paymentgateway.domain.model.PaymentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port de salida del dominio de pagos
 *define el contrato que debe implementar la infrastructure
 */
public interface PaymentRepository {
    //guardar pago en repositorio
    Payment save(Payment payment);

    // buscar por Id
    Optional<Payment> findById(UUID paymentId);

    //busca un pago por referencia unica
    Optional<Payment> findByPaymentReference(String paymentReference);

    // buscar pagos por id del cliente
    List<Payment> findByCustomerId(String customerId);

    //busca pagos por estado
    List<Payment> findByStatus(PaymentStatus status);

    //busca pagos por merchant
    List<Payment> findByMerchanId(String merchantId);

    //usca pagos por proveedor de pasarela
    List<Payment> findByGatewayProvider(String gatewayProvider);

    //buscar pago por id de transaccion de la pasarela
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

    //verifica si existe el pago con la referencia dada
    boolean existsByPaymentReference(String paymentReference);

    //elimina un pago
    void delete(Payment payment);

    //cuenta el total de pagos por estado
    long countByStatus(PaymentStatus status);
}
