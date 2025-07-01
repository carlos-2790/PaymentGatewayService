package com.paymentgateway.application.usecase;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.paymentgateway.application.port.in.ValidateCreditCardUseCase;
import com.paymentgateway.domain.model.CreditCardDetails;
import com.paymentgateway.domain.model.CreditCardValidationResult;

/*
 * implememntacion del caso de uso de validacion de tarjetas de credito
 */
@Service
public class ValidateCreditCardUseCaseImpl implements ValidateCreditCardUseCase {

    // patrones para diferentes tipos de tarjetas
    private static final Pattern VISA_PATTERN = Pattern.compile("^4[0-9]{12}(?:[0-9]{3})?$");
    private static final Pattern MASTERCARD_PATTERN = Pattern.compile("^5[1-5][0-9]{14}$");
    private static final Pattern AMEX_PATTERN = Pattern.compile("^3[47][0-9]{13}$");
    private static final Pattern DISCOVER_PATTERN = Pattern.compile("^6(?:011|5[0-9]{2})[0-9]{12}$");

    @Override
    public CreditCardValidationResult validateCreditCard(CreditCardDetails creditCardDetails) {
        try {
            // validar que la tarjeta no este expirada
            if (isExpired(creditCardDetails.expiryMonth(), creditCardDetails.expiryYear())) {
                return CreditCardValidationResult.expired(determineCardType(creditCardDetails.cardNumber()),
                        maskCardNumber(creditCardDetails.cardNumber()));
            }

            if (!isValidLuhn(creditCardDetails.cardNumber())) {
                return CreditCardValidationResult.invalid("Numero de tarjeta invalido");
            }

            String cardType = determineCardType(creditCardDetails.cardNumber());
            String maskedNumber = maskCardNumber(creditCardDetails.cardNumber());
            long daysUntilExpiry = calculateDaysUntilExpiry(creditCardDetails.expiryMonth(),
                    creditCardDetails.expiryYear());

            return CreditCardValidationResult.valid(cardType, maskedNumber, daysUntilExpiry);

        } catch (Exception e) {
            return CreditCardValidationResult.invalid("Error al validar la tarjeta: " + e.getMessage());
        }
    }

    @Override
    public String determineCardType(String cardNumber) {
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]+", "");
        if (VISA_PATTERN.matcher(cleanCardNumber).matches()) {
            return "VISA";
        } else if (MASTERCARD_PATTERN.matcher(cleanCardNumber).matches()) {
            return "MASTERCARD";
        } else if (AMEX_PATTERN.matcher(cleanCardNumber).matches()) {
            return "AMEX";
        } else if (DISCOVER_PATTERN.matcher(cleanCardNumber).matches()) {
            return "DISCOVER";
        }
        return "UNKNOWN";
    }

    // * Valida el número de tarjeta usando el algoritmo de Luhn
    // * @param cardNumber el número de tarjeta a validar
    // * @return true si el número de tarjeta es válido, false en caso contrario
    private boolean isValidLuhn(String cardNumber) {
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]+", "");
        int suma = 0;
        boolean alternar = false;

        for (int i = cleanCardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleanCardNumber.charAt(i));
            if (alternar) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            suma += digit;
            alternar = !alternar;
        }
        return suma % 10 == 0;
    }

    private String maskCardNumber(String cardNumber) {
        String cleanCardNumber = cardNumber.replaceAll("[\\s-]+", "");
        if (cleanCardNumber.length() < 4) {
            return "****-****-****-****";
        }
        String lastFour = cleanCardNumber.substring(cleanCardNumber.length() - 4);
        return "****-****-****-" + lastFour;
    }

    /*
     * Verifica si la tarjeta esta expirada
     */
    private boolean isExpired(String expiryMonth, String expiryYear) {
        try {
            int month = Integer.parseInt(expiryMonth);
            int year = Integer.parseInt(expiryYear);
            YearMonth expiryDate = YearMonth.of(year, month);
            return expiryDate.isBefore(YearMonth.now());
        } catch (Exception e) {
            return true;
        }
    }

    /*
     * Calcula los dias hasta la expiracion de la tarjeta
     */
    private long calculateDaysUntilExpiry(String expiryMonth, String expiryYear) {
        try {
            int month = Integer.parseInt(expiryMonth);
            int year = Integer.parseInt(expiryYear);
            
            // Crear fecha de expiración (último día del mes)
            YearMonth expiryYearMonth = YearMonth.of(year, month);
            java.time.LocalDate expiryDate = expiryYearMonth.atEndOfMonth();
            java.time.LocalDate currentDate = java.time.LocalDate.now();
            
            return ChronoUnit.DAYS.between(currentDate, expiryDate);
        } catch (Exception e) {
            return 0;
        }
    }
}
