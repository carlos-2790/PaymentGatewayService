import { APIRequestContext } from '@playwright/test';

/**
 * @helper ApiSetup
 * @description Configuración y setup común para pruebas de API
 */
export class ApiSetup {
  
  /**
   * Configura headers comunes para requests de API
   */
  static getCommonHeaders() {
    return {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'User-Agent': 'Playwright-API-Tests/1.0'
    };
  }

  /**
   * Verifica que el servicio esté disponible antes de ejecutar pruebas
   */
  static async verifyServiceHealth(request: APIRequestContext, baseURL: string) {
    const response = await request.get(`${baseURL}/api/v1/payments/health`);
    if (!response.ok()) {
      throw new Error(`Servicio no disponible. Status: ${response.status()}`);
    }
    console.log('✅ Servicio disponible');
    return response;
  }

  /**
   * Limpia datos de prueba después de cada test
   */
  static async cleanup(request: APIRequestContext) {
    // Implementar lógica de limpieza si es necesario
    console.log('Ejecutando limpieza post-test...');
  }

  /**
   * Configura datos de prueba iniciales
   */
  static async setupTestData(request: APIRequestContext) {
    // Implementar setup de datos si es necesario
    console.log('Configurando datos de prueba...');
  }
} 