package com.paymentgateway.shared.exception;

/**
 * Excepción personalizada para manejar errores relacionados con pagos.
 */
public class PaymentException extends RuntimeException {

    private final String code;

    public PaymentException(String message) {
        super(message);
        this.code = "PAYMENT_ERROR";
    }

    public PaymentException(String message, String code) {
        super(message);
        this.code = code;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.code = "PAYMENT_ERROR";
    }

    public PaymentException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
