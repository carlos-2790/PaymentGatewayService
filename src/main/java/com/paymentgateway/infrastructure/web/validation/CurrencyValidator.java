package com.paymentgateway.infrastructure.web.validation;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para códigos de moneda soportados
 */
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    // Monedas soportadas por el sistema (combinación de todas las gateways)
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "SEK", "NOK", "DKK"
    );

    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        // No necesita inicialización
    }

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        if (currency == null || currency.trim().isEmpty()) {
            return false; // Se maneja con @NotBlank
        }
        
        return SUPPORTED_CURRENCIES.contains(currency.toUpperCase());
    }
} 