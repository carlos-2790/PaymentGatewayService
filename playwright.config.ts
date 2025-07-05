import { defineConfig, devices } from '@playwright/test';

/**
 * Configuración de Playwright para pruebas de API
 * @see https://playwright.dev/docs/test-configuration
 */

export default defineConfig({
  testDir: './tests/api',
  /* Run tests in files in parallel */
  fullyParallel: true,
  /* Fallar la build en CI si dejaste test.only por accidente */
  forbidOnly: !!process.env.CI,
  /* Reintentar en CI solamente */
  retries: process.env.CI ? 2 : 0,
  /* Optar por menos workers en CI */
  workers: process.env.CI ? 1 : undefined,
  /* Reporter para generar reportes HTML */
  reporter: [
    ['html'],
    ['json', { outputFile: 'test-results/results.json' }],
    ['junit', { outputFile: 'test-results/junit.xml' }]
  ],
  /* Configuración compartida para todas las pruebas */
  use: {
    /* URL base para usar en actions como `await page.goto('/')` */
    baseURL: process.env.API_BASE_URL || 'http://localhost:8080',
    /* Recopilar trace en primera reintento de una prueba fallida */
    trace: 'on-first-retry',
    /* Configuración específica para API */
    extraHTTPHeaders: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
    },
  },

  /* Configurar proyectos para diferentes navegadores/entornos */
  projects: [
    {
      name: 'api-tests',
      testDir: './tests/api',
      use: {
        ...devices['Desktop Chrome'],
        // Configuración específica para pruebas de API
        baseURL: process.env.API_BASE_URL || 'http://localhost:8080',
      },
    },
  ],

  /* Ejecutar servidor local antes de iniciar las pruebas */
  webServer: {
    command: 'mvn spring-boot:run -Dspring.profiles.active=ci',
    url: 'http://localhost:8080/actuator/health',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },
}); 