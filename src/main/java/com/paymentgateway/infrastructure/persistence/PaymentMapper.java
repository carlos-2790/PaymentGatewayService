package com.paymentgateway.infrastructure.persistence;

import org.springframework.stereotype.Component;

import com.paymentgateway.domain.model.Payment;

@Component
public class PaymentMapper {
    public PaymentEntity toEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .gateWayProvider(payment.getGatewayProvider())
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .customerId(payment.getCustomerId())
                .merchantId(payment.getMerchantId())
                .description(payment.getDescription())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .completedAt(payment.getCompletedAt())
                .version(payment.getVersion())
                .build();
    }

    public Payment toDomain(PaymentEntity entity) {
        return Payment.builder()
                .id(entity.getId())
                .paymentReference(entity.getPaymentReference())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .status(entity.getStatus())
                .paymentMethod(entity.getPaymentMethod())
                .gatewayProvider(entity.getGateWayProvider())
                .gatewayTransactionId(entity.getGatewayTransactionId())
                .customerId(entity.getCustomerId())
                .merchantId(entity.getMerchantId())
                .description(entity.getDescription())
                .failureReason(entity.getFailureReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .completedAt(entity.getCompletedAt())
                .version(entity.getVersion())
                .build();
    }
}
