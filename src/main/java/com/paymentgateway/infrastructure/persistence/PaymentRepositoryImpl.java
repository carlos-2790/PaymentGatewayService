package com.paymentgateway.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.paymentgateway.domain.model.Payment;
import com.paymentgateway.domain.model.PaymentStatus;
import com.paymentgateway.domain.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentMapper mapper;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = mapper.toEntity(payment);
        PaymentEntity saved = paymentJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return paymentJpaRepository.findById(paymentId)
                .map(mapper::toDomain);
    }

    //busca un pago por referencia unica
    @Override
    public Optional<Payment> findByPaymentReference(String paymentReference) {
        return paymentJpaRepository.findByPaymentReference(paymentReference)
                .map(mapper::toDomain);
    }

    // buscar pagos por id del cliente
    @Override
    public List<Payment> findByCustomerId(String customerId) {
        return paymentJpaRepository.findByMerchantId(customerId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    //busca pagos por estado
    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentJpaRepository.findByStatus(status)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    //busca pagos por merchant
    @Override
    public List<Payment> findByMerchanId(String merchantId) {
        return paymentJpaRepository.findByMerchantId(merchantId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    //busca pagos por proveedor de pasarela
    @Override
    public List<Payment> findByGatewayProvider(String gatewayProvider) {

        return paymentJpaRepository.findByGateWayProvider(gatewayProvider)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    //buscar pago por id de transaccion de la pasarela
    @Override
    public Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId) {
        return paymentJpaRepository.findByGatewayTransactionId(gatewayTransactionId)
                .map(mapper::toDomain);
    }

    //verifica si existe el pago con la referencia dada
    @Override
    public boolean existsByPaymentReference(String paymentReference) {
        return paymentJpaRepository.existsByPaymentReference(paymentReference);
    }

    //elimina un pago
    @Override
    public void delete(Payment payment) {
        paymentJpaRepository.delete(mapper.toEntity(payment));
    }

    //cuenta el total de pagos por estado
    @Override
    public long countByStatus(PaymentStatus status) {
        return paymentJpaRepository.countByStatus(status);
    }

}
