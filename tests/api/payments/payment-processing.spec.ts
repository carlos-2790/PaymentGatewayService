import { expect, test } from '@playwright/test';
import { PaymentRequestFactory } from '../shared/factories';
import { ApiSetup } from '../shared/setup';
import { ApiValidators } from '../shared/validators';

test.describe('Payment Processing API', () => {
  
  test.beforeEach(async ({ request }) => {
    await ApiSetup.verifyServiceHealth(request, 'http://localhost:8080');
  });

  test('debería procesar pago con tarjeta de crédito exitosamente', async ({ request }) => {
    // Arrange
    const paymentData = PaymentRequestFactory.createCreditCardPayment();
    
    // Act
    const response = await request.post('/api/v1/payments', {
      data: paymentData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.ok()).toBeTruthy();
    expect(response.status()).toBe(200);
    
    const responseBody = await response.json();
    ApiValidators.validateSuccessfulPaymentResponse(responseBody);
    expect(responseBody.paymentReference).toBe(paymentData.paymentReference);
    expect(responseBody.amount).toBe(paymentData.amount);
    expect(responseBody.currency).toBe(paymentData.currency);
  });

  test('debería procesar pago con PayPal exitosamente', async ({ request }) => {
    // Arrange
    const paymentData = PaymentRequestFactory.createPayPalPayment();
    
    // Act
    const response = await request.post('/api/v1/payments', {
      data: paymentData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseBody = await response.json();
    ApiValidators.validateSuccessfulPaymentResponse(responseBody);
    expect(responseBody.paymentMethod).toBe('PAYPAL');
  });

  test('debería rechazar pago con datos inválidos', async ({ request }) => {
    // Arrange
    const invalidPaymentData = PaymentRequestFactory.createInvalidPayment();
    
    // Act
    const response = await request.post('/api/v1/payments', {
      data: invalidPaymentData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.status()).toBe(400);
    const responseBody = await response.json();
    ApiValidators.validateErrorResponse(responseBody, 400);
    expect(responseBody.error).toBe('Bad Request');
  });

  test('debería rechazar pago con monto negativo', async ({ request }) => {
    // Arrange
    const paymentData = PaymentRequestFactory.createCreditCardPayment({ 
      amount: -50.00 
    });
    
    // Act
    const response = await request.post('/api/v1/payments', {
      data: paymentData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.status()).toBe(400);
    const responseBody = await response.json();
    ApiValidators.validateErrorResponse(responseBody, 400);
    expect(responseBody.error).toBe('Bad Request');
  });

  test('debería rechazar pago con referencia vacía', async ({ request }) => {
    // Arrange
    const paymentData = PaymentRequestFactory.createCreditCardPayment({ 
      paymentReference: "" 
    });
    
    // Act
    const response = await request.post('/api/v1/payments', {
      data: paymentData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.status()).toBe(400);
    const responseBody = await response.json();
    ApiValidators.validateErrorResponse(responseBody, 400);
  });

  test('debería rechazar pago con moneda inválida', async ({ request }) => {
    // Arrange
    const paymentData = PaymentRequestFactory.createCreditCardPayment({ 
      currency: "INVALID_CURRENCY" 
    });
    
    // Act
    const response = await request.post('/api/v1/payments', {
      data: paymentData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.status()).toBe(400);
    const responseBody = await response.json();
    ApiValidators.validateErrorResponse(responseBody, 400);
  });
});

test.describe('Payment Health Check', () => {
  
  test('debería responder health check correctamente', async ({ request }) => {
    // Act
    const response = await request.get('/api/v1/payments/health');
    
    // Assert
    expect(response.ok()).toBeTruthy();
    expect(response.status()).toBe(200);
    
    const responseText = await response.text();
    expect(responseText).toBe('Payment service is up and running!');
  });
});

test.describe('Payment Error Handling', () => {
  
  test('debería manejar payload malformado', async ({ request }) => {
    // Arrange - JSON malformado
    const malformedPayload = '{"amount": "invalid_number"}';
    
    // Act
    const response = await request.post('/api/v1/payments', {
      data: malformedPayload,
      headers: {
        ...ApiSetup.getCommonHeaders(),
        'Content-Type': 'application/json'
      }
    });
    
    // Assert
    expect(response.status()).toBe(400);
    const responseBody = await response.json();
    ApiValidators.validateErrorResponse(responseBody, 400);
  });

  test('debería procesar request sin Content-Type (Spring Boot lo infiere)', async ({ request }) => {
    // Arrange
    const paymentData = PaymentRequestFactory.createCreditCardPayment();
    
    // Act - sin Content-Type header explícito
    const response = await request.post('/api/v1/payments', {
      data: paymentData,
      headers: {
        'Accept': 'application/json'
        // Sin Content-Type - Spring Boot lo infiere automáticamente
      }
    });
    
    // Assert - Spring Boot procesa el request correctamente
    expect(response.status()).toBe(200);
    const responseBody = await response.json();
    ApiValidators.validateSuccessfulPaymentResponse(responseBody);
  });

  test('debería rechazar request con Content-Type incorrecto', async ({ request }) => {
    // Arrange
    const paymentData = PaymentRequestFactory.createCreditCardPayment();
    
    // Act - con Content-Type explícitamente incorrecto
    const response = await request.post('/api/v1/payments', {
      data: JSON.stringify(paymentData),
      headers: {
        'Content-Type': 'text/plain',
        'Accept': 'application/json'
      }
    });
    
    // Assert - esto debería fallar
    expect(response.status()).toBe(415); // Unsupported Media Type
    const responseBody = await response.json();
    ApiValidators.validateErrorResponse(responseBody, 415);
  });

  test('debería rechazar request vacío', async ({ request }) => {
    // Act - request sin body
    const response = await request.post('/api/v1/payments', {
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.status()).toBe(400);
    const responseBody = await response.json();
    ApiValidators.validateErrorResponse(responseBody, 400);
  });

  test('debería rechazar request con JSON vacío', async ({ request }) => {
    // Act - request con objeto vacío
    const response = await request.post('/api/v1/payments', {
      data: {},
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.status()).toBe(400);
    const responseBody = await response.json();
    ApiValidators.validateErrorResponse(responseBody, 400);
  });
}); 