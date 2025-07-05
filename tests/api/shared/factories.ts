/**
 * @helper PaymentRequestFactory
 * @description Factory para crear requests de pago válidos para testing
 * @returns {Object} Request de pago configurado
 */
export class PaymentRequestFactory {
  static createCreditCardPayment(overrides: Partial<any> = {}) {
    return {
      paymentReference: `test-ref-${Date.now()}`,
      amount: 100.50,
      currency: "USD",
      paymentMethod: "CREDIT_CARD",
      customerId: "cust-123",
      merchantId: "merch-456",
      description: "Test Payment",
      paymentDetails: {
        type: "CREDIT_CARD",
        cardNumber: "4242424242424242",
        expiryMonth: "12",
        expiryYear: "2028",
        cvv: "123",
        cardHolderName: "Juan Perez"
      },
      ...overrides
    };
  }

  static createPayPalPayment(overrides: Partial<any> = {}) {
    return {
      paymentReference: `paypal-ref-${Date.now()}`,
      amount: 250.75,
      currency: "EUR",
      paymentMethod: "PAYPAL",
      customerId: "cust-456",
      merchantId: "merch-789",
      description: "PayPal Payment",
      paymentDetails: {
        type: "PAYPAL",
        email: "user@example.com",
        returnUrl: "https://merchant.com/success",
        cancelUrl: "https://merchant.com/cancel"
      },
      ...overrides
    };
  }

  /**
   * Crea datos inválidos que DEBEN ser rechazados por la API
   */
  static createInvalidPayment(overrides: Partial<any> = {}) {
    return {
      paymentReference: "", // Vacío - DEBE fallar
      amount: -100, // Negativo - DEBE fallar
      currency: "INVALID", // Inválido - DEBE fallar
      paymentMethod: "UNKNOWN", // Desconocido - DEBE fallar
      customerId: "", // Vacío - DEBE fallar
      merchantId: "", // Vacío - DEBE fallar
      paymentDetails: null, // Nulo - DEBE fallar
      ...overrides
    };
  }

  /**
   * Casos específicos de validación que DEBEN fallar
   */
  static createNegativeAmountPayment() {
    return this.createCreditCardPayment({ amount: -50.00 });
  }

  static createZeroAmountPayment() {
    return this.createCreditCardPayment({ amount: 0 });
  }

  static createEmptyReferencePayment() {
    return this.createCreditCardPayment({ paymentReference: "" });
  }

  static createNullReferencePayment() {
    return this.createCreditCardPayment({ paymentReference: null });
  }

  static createInvalidCurrencyPayment() {
    return this.createCreditCardPayment({ currency: "XYZ" });
  }

  static createInvalidPaymentMethodPayment() {
    return this.createCreditCardPayment({ paymentMethod: "BITCOIN" });
  }

  static createMissingPaymentDetailsPayment() {
    const payment = this.createCreditCardPayment();
    delete (payment as any).paymentDetails;
    return payment;
  }
}

/**
 * @helper CreditCardFactory
 * @description Factory para crear datos de tarjetas de crédito para testing
 * @returns {Object} Datos de tarjeta configurados
 */
export class CreditCardFactory {
  static createValidVisa(overrides: Partial<any> = {}) {
    return {
      cardNumber: "4242424242424242",
      expiryMonth: "12",
      expiryYear: "2028",
      cvv: "123",
      cardHolderName: "Juan Perez",
      ...overrides
    };
  }

  static createValidMastercard(overrides: Partial<any> = {}) {
    return {
      cardNumber: "5555555555554444",
      expiryMonth: "08",
      expiryYear: "2027",
      cvv: "456",
      cardHolderName: "Maria Garcia",
      ...overrides
    };
  }

  static createValidAmex(overrides: Partial<any> = {}) {
    return {
      cardNumber: "378282246310005",
      expiryMonth: "06",
      expiryYear: "2026",
      cvv: "1234",
      cardHolderName: "Carlos Rodriguez",
      ...overrides
    };
  }

  /**
   * Casos que DEBEN ser rechazados por validación
   */
  static createInvalidCard(overrides: Partial<any> = {}) {
    return {
      cardNumber: "1234567890123456", // Número inválido
      expiryMonth: "13", // Mes inválido
      expiryYear: "2020", // Año pasado
      cvv: "12345", // CVV muy largo
      cardHolderName: "", // Nombre vacío
      ...overrides
    };
  }

  static createExpiredCard(overrides: Partial<any> = {}) {
    return {
      cardNumber: "4242424242424242",
      expiryMonth: "01",
      expiryYear: "2020", // Definitivamente expirada
      cvv: "123",
      cardHolderName: "Expired User",
      ...overrides
    };
  }

  // Casos específicos que DEBEN fallar
  static createEmptyCardNumber() {
    return this.createValidVisa({ cardNumber: "" });
  }

  static createNullCardNumber() {
    return this.createValidVisa({ cardNumber: null });
  }

  static createInvalidMonth() {
    return this.createValidVisa({ expiryMonth: "13" });
  }

  static createInvalidYear() {
    return this.createValidVisa({ expiryYear: "20" }); // Año con 2 dígitos
  }

  static createEmptyName() {
    return this.createValidVisa({ cardHolderName: "" });
  }

  static createEmptyCvv() {
    return this.createValidVisa({ cvv: "" });
  }
} 