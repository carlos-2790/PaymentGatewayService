package com.paymentgateway.domain.model;

/**
 * Datos específicos del gateway de pago
 */
public record GatewaySpecificData(String providerId, String rawResponse, String fees, String additionalInfo) {}
