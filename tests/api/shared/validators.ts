import { expect } from '@playwright/test';

/**
 * @helper ApiValidators
 * @description Utilidades de validación comunes para respuestas de API
 */
export class ApiValidators {
  
  /**
   * Valida estructura básica de respuesta de pago exitoso
   */
  static validateSuccessfulPaymentResponse(response: any) {
    expect(response).toHaveProperty('id');
    expect(response).toHaveProperty('paymentReference');
    expect(response).toHaveProperty('amount');
    expect(response).toHaveProperty('currency');
    expect(response).toHaveProperty('status');
    expect(response).toHaveProperty('paymentMethod');
    expect(response).toHaveProperty('gatewayProvider');
    expect(response).toHaveProperty('createdAt');
    expect(response).toHaveProperty('updatedAt');
    
    // Validar tipos
    expect(typeof response.id).toBe('string');
    expect(typeof response.amount).toBe('number');
    expect(typeof response.paymentReference).toBe('string');
    expect(['COMPLETED', 'PENDING', 'FAILED']).toContain(response.status);
  }

  /**
   * Valida estructura de respuesta de validación de tarjeta
   */
  static validateCreditCardValidationResponse(response: any) {
    expect(response).toHaveProperty('isValid');
    expect(response).toHaveProperty('cardType');
    expect(response).toHaveProperty('maskedCardNumber');
    expect(response).toHaveProperty('message');
    expect(response).toHaveProperty('isExpired');
    expect(response).toHaveProperty('daysUntilExpiry');
    
    // Validar tipos
    expect(typeof response.isValid).toBe('boolean');
    expect(typeof response.cardType).toBe('string');
    expect(typeof response.maskedCardNumber).toBe('string');
    expect(typeof response.message).toBe('string');
    expect(typeof response.isExpired).toBe('boolean');
    expect(typeof response.daysUntilExpiry).toBe('number');
  }

  /**
   * Valida estructura de respuesta de error estándar de Spring Boot
   */
  static validateErrorResponse(response: any, expectedStatus: number) {
    expect(response).toHaveProperty('timestamp');
    expect(response).toHaveProperty('status');
    expect(response).toHaveProperty('error');
    expect(response).toHaveProperty('path');
    
    // Validar tipos
    expect(typeof response.timestamp).toBe('string');
    expect(typeof response.status).toBe('number');
    expect(typeof response.error).toBe('string');
    expect(typeof response.path).toBe('string');
    
    expect(response.status).toBe(expectedStatus);
  }

  /**
   * Valida estructura de respuesta de error con mensaje personalizado
   * Algunos errores pueden incluir un campo 'message' adicional
   */
  static validateErrorResponseWithMessage(response: any, expectedStatus: number) {
    // Validar estructura básica
    ApiValidators.validateErrorResponse(response, expectedStatus);
    
    // Validar mensaje si existe
    if (response.message) {
      expect(typeof response.message).toBe('string');
    }
  }

  /**
   * Valida que una respuesta tenga el formato UUID válido
   */
  static validateUUID(uuid: string) {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    expect(uuid).toMatch(uuidRegex);
  }

  /**
   * Valida formato de timestamp ISO
   */
  static validateISOTimestamp(timestamp: string) {
    const date = new Date(timestamp);
    expect(date.toISOString()).toBe(timestamp);
  }

  /**
   * Valida respuesta de error y opcionalmente verifica contenido del mensaje de error
   */
  static validateErrorResponseWithContent(response: any, expectedStatus: number, expectedErrorContent?: string) {
    ApiValidators.validateErrorResponse(response, expectedStatus);
    
    if (expectedErrorContent) {
      // Buscar el contenido en el campo 'error' ya que 'message' no existe
      expect(response.error.toLowerCase()).toContain(expectedErrorContent.toLowerCase());
    }
  }
} 