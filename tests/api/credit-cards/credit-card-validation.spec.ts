import { expect, test } from '@playwright/test';
import { CreditCardFactory } from '../shared/factories';
import { ApiSetup } from '../shared/setup';
import { ApiValidators } from '../shared/validators';

test.describe('Credit Card Validation API', () => {
  
  test.beforeEach(async ({ request }) => {
    await ApiSetup.verifyServiceHealth(request, 'http://localhost:8080');
  });

  test('debería validar tarjeta VISA correctamente', async ({ request }) => {
    // Arrange
    const cardData = CreditCardFactory.createValidVisa();
    
    // Act
    const response = await request.post('/api/v1/credit-cards/validate', {
      data: cardData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseBody = await response.json();
    ApiValidators.validateCreditCardValidationResponse(responseBody);
    expect(responseBody.isValid).toBe(true);
    expect(responseBody.cardType).toBe('VISA');
    expect(responseBody.maskedCardNumber).toContain('****');
  });

  test('debería validar tarjeta MASTERCARD correctamente', async ({ request }) => {
    // Arrange
    const cardData = CreditCardFactory.createValidMastercard();
    
    // Act
    const response = await request.post('/api/v1/credit-cards/validate', {
      data: cardData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseBody = await response.json();
    expect(responseBody.isValid).toBe(true);
    expect(responseBody.cardType).toBe('MASTERCARD');
  });

  test('debería validar tarjeta AMEX correctamente', async ({ request }) => {
    // Arrange
    const cardData = CreditCardFactory.createValidAmex();
    
    // Act
    const response = await request.post('/api/v1/credit-cards/validate', {
      data: cardData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseBody = await response.json();
    expect(responseBody.isValid).toBe(true);
    expect(responseBody.cardType).toBe('AMEX');
  });

  test('debería rechazar tarjeta inválida', async ({ request }) => {
    // Arrange
    const cardData = CreditCardFactory.createInvalidCard();
    
    // Act
    const response = await request.post('/api/v1/credit-cards/validate', {
      data: cardData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseBody = await response.json();
    expect(responseBody.isValid).toBe(false);
    expect(responseBody.message).toContain('Card holder name cannot be null or empty');
  });

  test('debería rechazar tarjeta definitivamente expirada', async ({ request }) => {
    // Arrange - usar tarjeta que falla en el constructor
    const cardData = CreditCardFactory.createExpiredCard();
    
    // Act
    const response = await request.post('/api/v1/credit-cards/validate', {
      data: cardData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert - el controlador debería capturar la excepción y devolver resultado inválido
    expect(response.ok()).toBeTruthy();
    const responseBody = await response.json();
    expect(responseBody.isValid).toBe(false);
    expect(responseBody.message).toContain('expirado');
  });

  test('debería detectar tarjeta que expira en el mes actual', async ({ request }) => {
    // Arrange - crear tarjeta que expira exactamente hoy
    const today = new Date();
    const currentMonth = String(today.getMonth() + 1).padStart(2, '0');
    const currentYear = String(today.getFullYear());
    
    const cardData = {
      cardNumber: "4242424242424242",
      expiryMonth: currentMonth,
      expiryYear: currentYear,
      cvv: "123",
      cardHolderName: "Current Month User"
    };
    
    // Act
    const response = await request.post('/api/v1/credit-cards/validate', {
      data: cardData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseBody = await response.json();
    
    // La tarjeta podría ser válida o expirada dependiendo del día del mes
    // Verificamos que la respuesta sea consistente
    if (responseBody.isExpired) {
      expect(responseBody.isValid).toBe(false);
      expect(responseBody.daysUntilExpiry).toBeLessThanOrEqual(0);
      expect(responseBody.message).toContain('expirada');
    } else {
      expect(responseBody.isValid).toBe(true);
      expect(responseBody.daysUntilExpiry).toBeGreaterThanOrEqual(0);
    }
  });
});

test.describe('Credit Card Type Detection', () => {
  
  test('debería detectar tipo VISA por número de tarjeta', async ({ request }) => {
    // Act
    const response = await request.get('/api/v1/credit-cards/card-type/4242424242424242');
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseText = await response.text();
    expect(responseText).toBe('VISA');
  });

  test('debería detectar tipo MASTERCARD por número de tarjeta', async ({ request }) => {
    // Act
    const response = await request.get('/api/v1/credit-cards/card-type/5555555555554444');
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseText = await response.text();
    expect(responseText).toBe('MASTERCARD');
  });

  test('debería detectar tipo AMEX por número de tarjeta', async ({ request }) => {
    // Act
    const response = await request.get('/api/v1/credit-cards/card-type/378282246310005');
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseText = await response.text();
    expect(responseText).toBe('AMEX');
  });

  test('debería retornar UNKNOWN para tarjeta no reconocida', async ({ request }) => {
    // Act
    const response = await request.get('/api/v1/credit-cards/card-type/1234567890123456');
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseText = await response.text();
    expect(responseText).toBe('UNKNOWN');
  });
});

test.describe('Credit Card Edge Cases', () => {
  
  test('debería manejar tarjeta con formato de fecha inválido', async ({ request }) => {
    // Arrange
    const cardData = {
      cardNumber: "4242424242424242",
      expiryMonth: "13", // Mes inválido
      expiryYear: "2025",
      cvv: "123",
      cardHolderName: "Invalid Month User"
    };
    
    // Act
    const response = await request.post('/api/v1/credit-cards/validate', {
      data: cardData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseBody = await response.json();
    expect(responseBody.isValid).toBe(false);
    expect(responseBody.message).toContain('between 01 and 12');
  });

  test('debería manejar tarjeta con CVV inválido', async ({ request }) => {
    // Arrange
    const cardData = {
      cardNumber: "4242424242424242",
      expiryMonth: "12",
      expiryYear: "2025",
      cvv: "", // CVV vacío
      cardHolderName: "Empty CVV User"
    };
    
    // Act
    const response = await request.post('/api/v1/credit-cards/validate', {
      data: cardData,
      headers: ApiSetup.getCommonHeaders()
    });
    
    // Assert
    expect(response.ok()).toBeTruthy();
    const responseBody = await response.json();
    expect(responseBody.isValid).toBe(false);
    expect(responseBody.message).toContain('CVV cannot be null or empty');
  });
}); 