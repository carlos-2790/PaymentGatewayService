# Payment Gateway Service 💳

Un servicio de pasarelas de pago desarrollado con **Spring Boot** y **Arquitectura Hexagonal**, que integra múltiples proveedores de pago como Stripe y PayPal.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Configuración del Proyecto](#-configuración-del-proyecto)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [Configuración de Docker](#-configuración-de-docker)
- [Variables de Entorno](#-variables-de-entorno)
- [API Documentation](#-api-documentation)
- [Testing](#-testing)
- [Validación y Manejo de Errores](#-validación-y-manejo-de-errores)
- [Arquitectura](#-arquitectura)
- [Contribución](#-contribución)

## ✨ Características

- 🏗️ **Arquitectura Hexagonal**: Separación clara de responsabilidades
- 💳 **Múltiples Pasarelas**: Soporte para Stripe y PayPal
- 🔒 **Seguridad**: Implementación de Spring Security con JWT
- 📊 **Observabilidad**: Métricas con Prometheus y trazabilidad
- 🐘 **Base de Datos**: PostgreSQL con migraciones Flyway
- ⚡ **Cache**: Redis para optimización de rendimiento
- 📚 **Documentación**: Swagger/OpenAPI integrado
- 🧪 **Testing**: Cobertura completa con TestContainers
- ✅ **Validación Robusta**: Sistema completo de validación de entrada
- 🚨 **Manejo de Errores**: Respuestas HTTP consistentes y descriptivas

## 🛠️ Tecnologías

- **Java 21**
- **Spring Boot 3.2.1**
- **Spring Security**
- **Spring Data JPA**
- **Bean Validation (JSR-303)**
- **PostgreSQL 14**
- **Redis 7**
- **Maven**
- **Docker & Docker Compose**
- **Flyway**
- **Swagger/OpenAPI**
- **Lombok**
- **MapStruct**
- **Playwright** (para tests E2E)

## 📋 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- ☕ **Java 21** o superior
- 🔧 **Maven 3.6+**
- 🐳 **Docker** y **Docker Compose**
- 🔑 Cuentas de desarrollador en:
  - [Stripe](https://stripe.com/docs/keys) (para obtener API keys)
  - [PayPal Developer](https://developer.paypal.com/) (para obtener Client ID y Secret)

## ⚙️ Configuración del Proyecto

### 1. Clonar el Repositorio

```bash
git clone <url-del-repositorio>
cd PaymentGatewayService
```

### 2. Configurar Variables de Entorno

Crea un archivo `.env` en la raíz del proyecto:

```env
# Stripe Configuration
STRIPE_SECRET_KEY=sk_test_tu_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=pk_test_tu_stripe_publishable_key

# PayPal Configuration
PAYPAL_CLIENT_ID=tu_paypal_client_id
PAYPAL_CLIENT_SECRET=tu_paypal_client_secret
PAYPAL_ENVIRONMENT=sandbox

# Database Configuration
DB_USERNAME=payment_user
DB_PASSWORD=payment_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

## 🚀 Instalación y Ejecución

### Opción 1: Ejecución con Docker Descktop(Recomendado)

#### 1. Levantar los Servicios de Infraestructura

```bash
# Levantar PostgreSQL y Redis
docker compose up -d
```

#### 2. Compilar y Ejecutar la Aplicación

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn spring-boot:run
```

O usando los scripts de npm:

```bash
# Levantar infraestructura
npm run docker:compose:up

# Ejecutar aplicación
npm start
```

### Opción 2: Ejecución Completa con Docker

Si prefieres ejecutar todo en contenedores, puedes crear un Dockerfile:

```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/payment-gateway-service-1.0.0-SNAPSHOT.jar"]
```

Luego agregar el servicio al `docker-compose.yml`:

```yaml
  payment-service:
    build: .
    container_name: payment-service
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/payment_gateway
      - SPRING_DATA_REDIS_HOST=redis
```

## 🐳 Configuración de Docker

### Comandos Útiles de Docker

```bash
# Levantar todos los servicios en segundo plano
docker compose up -d

# Ver logs de los servicios
docker compose logs -f

# Parar todos los servicios
docker compose down

# Parar y eliminar volúmenes (⚠️ Elimina datos)
docker compose down -v

# Reconstruir servicios
docker compose up --build

# Ver estado de los contenedores
docker compose ps
```

### Servicios Disponibles

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **payment-service** | 8080 | Aplicación principal |
| **postgres** | 5432 | Base de datos PostgreSQL |
| **redis** | 6379 | Cache Redis |

## 🔧 Variables de Entorno

### Variables Requeridas

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `STRIPE_SECRET_KEY` | Clave secreta de Stripe | `sk_test_your_stripe_secret_key` |
| `STRIPE_PUBLISHABLE_KEY` | Clave pública de Stripe | `pk_test_your_stripe_publishable_key` |
| `PAYPAL_CLIENT_ID` | Client ID de PayPal | `your_paypal_client_id` |
| `PAYPAL_CLIENT_SECRET` | Client Secret de PayPal | `your_paypal_client_secret` |

### Variables Opcionales

| Variable | Descripción | Valor por Defecto |
|----------|-------------|-------------------|
| `DB_USERNAME` | Usuario de PostgreSQL | `payment_user` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `payment_password` |
| `REDIS_HOST` | Host de Redis | `localhost` |
| `REDIS_PORT` | Puerto de Redis | `6379` |
| `PAYPAL_ENVIRONMENT` | Entorno de PayPal | `sandbox` |

## 📚 API Documentation

### 🎯 Swagger UI

Una vez que la aplicación esté ejecutándose, puedes acceder a la documentación interactiva de la API:

**🔗 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

### Endpoints Principales

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/payments` | Procesar un pago |
| `GET` | `/api/v1/payments/health` | Health check del servicio |
| `POST` | `/api/v1/credit-cards/validate` | Validar tarjeta de crédito |

### Ejemplo de Request

```json
{
  "paymentReference": "test-ref-123",
  "amount": 100.50,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "customerId": "cust-123",
  "merchantId": "merch-456",
  "description": "Test Payment",
  "paymentDetails": {
    "type": "CREDIT_CARD",
    "cardNumber": "4242424242424242",
    "expiryMonth": "12",
    "expiryYear": "2028",
    "cvv": "123",
    "cardHolderName": "Juan Perez"
  }
}
```

## 🧪 Testing

### Ejecutar Tests

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests con reporte de cobertura
mvn test jacoco:report

# Ejecutar solo tests unitarios
mvn test -Dtest="*Test"

# Ejecutar solo tests de integración
mvn test -Dtest="*IT"

# Ejecutar tests E2E con Playwright
npx playwright test
```

### Tests Disponibles

- ✅ **Tests Unitarios**: Lógica de dominio y casos de uso
- ✅ **Tests de Integración**: Controllers y repositorios
- ✅ **Tests de Arquitectura**: Validación de principios hexagonales
- ✅ **Tests E2E**: API completa con Playwright
- ✅ **TestContainers**: Tests con base de datos real

### 🧪 Tests E2E

Los tests E2E (End-to-End) verifican la funcionalidad completa de la API:

#### Ejecución Automática (Recomendado)

**En Windows:**
```powershell
# Ejecutar todos los tests E2E con configuración automática
.\scripts\run-api-tests.ps1

# Ejecutar con environment específico
.\scripts\run-api-tests.ps1 -Environment "staging"
```

**En Linux/macOS:**
```bash
# Ejecutar todos los tests E2E con configuración automática
./scripts/run-api-tests.sh

# Ejecutar con environment específico
./scripts/run-api-tests.sh staging
```

Los scripts automáticos incluyen:
- ✅ Verificación de dependencias
- ✅ Inicio de servicios Docker (PostgreSQL, Redis)
- ✅ Compilación de la aplicación
- ✅ Instalación de dependencias Node.js
- ✅ Instalación de Playwright browsers
- ✅ Inicio de la aplicación Spring Boot
- ✅ Ejecución de tests E2E
- ✅ Limpieza automática de procesos
- ✅ Generación de reportes HTML

#### Ejecución Manual

```bash
# 1. Iniciar servicios de infraestructura
docker-compose up -d postgres redis

# 2. Iniciar aplicación Spring Boot
mvn spring-boot:run -Dspring.profiles.active=test

# 3. Instalar dependencias de Playwright
npm install
npx playwright install

# 4. Ejecutar tests E2E
npx playwright test tests/api/

# 5. Ejecutar tests específicos
npx playwright test tests/api/payments/
npx playwright test tests/api/credit-cards/

# 6. Ver reporte HTML
npx playwright show-report
```

### Estructura de Tests E2E

```
tests/
├── api/
│   ├── payments/
│   │   └── payment-processing.spec.ts
│   ├── credit-cards/
│   │   └── credit-card-validation.spec.ts
│   └── shared/
│       ├── factories.ts      # Factories para datos de test
│       ├── setup.ts          # Configuración común
│       └── validators.ts     # Validadores reutilizables
```

## ✅ Validación y Manejo de Errores

### 🛡️ Sistema de Validación Robusto

El servicio implementa un sistema completo de validación en múltiples capas:

#### **1. Validación de Entrada (Bean Validation)**

```java
public record PaymentRequestDTO(
    @NotBlank(message = "Payment reference is required")
    String paymentReference,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    BigDecimal amount,
    
    @NotBlank(message = "Currency is required")
    @ValidCurrency(message = "Currency not supported")
    String currency,
    
    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,
    
    @NotBlank(message = "Customer ID is required")
    String customerId,
    
    @NotBlank(message = "Merchant ID is required")
    String merchantId,
    
    @NotNull(message = "Payment details are required")
    PaymentDetails paymentDetails
) { }
```

#### **2. Validaciones Personalizadas**

**Validador de Monedas (`@ValidCurrency`)**:
```java
@ValidCurrency(message = "Currency not supported")
String currency;
```

Monedas soportadas:
- `USD`, `EUR`, `GBP`, `JPY`, `CAD`, `AUD`, `CHF`, `SEK`, `NOK`, `DKK`

#### **3. Validación de Dominio**

Las entidades de dominio incluyen validaciones de negocio:

```java
public record PaymentRequest(...) {
    public PaymentRequest {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        // Más validaciones...
    }
}
```

### 🚨 Manejo Global de Errores

El sistema cuenta con un `@ControllerAdvice` que maneja todos los tipos de errores:

#### **Tipos de Errores Manejados**

| Excepción | Código HTTP | Descripción |
|-----------|-------------|-------------|
| `IllegalArgumentException` | 400 | Errores de validación de dominio |
| `MethodArgumentNotValidException` | 400 | Errores de Bean Validation |
| `HttpMessageNotReadableException` | 400 | JSON malformado o tipos incorrectos |
| `HttpMediaTypeNotSupportedException` | 415 | Content-Type no soportado |
| `PaymentException` | 400 | Errores específicos de pagos |
| `UnsupportedOperationException` | 400 | Operaciones no soportadas |
| `Exception` | 500 | Errores generales no capturados |

#### **Formato de Respuesta de Error**

```json
{
  "timestamp": "2025-07-02T15:54:59.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid value for field 'paymentMethod': UNKNOWN",
  "path": "/api/v1/payments"
}
```

### 📋 Casos de Validación Cubiertos

#### **✅ Casos que DEBEN ser rechazados (HTTP 400)**

1. **Referencia de pago**:
   - Vacía o nula
   - Solo espacios en blanco

2. **Monto**:
   - Negativo
   - Cero
   - Nulo

3. **Moneda**:
   - Código inválido (ej: `"INVALID_CURRENCY"`)
   - Vacía o nula

4. **Método de pago**:
   - Enum inválido (ej: `"UNKNOWN"`)
   - Nulo

5. **IDs de cliente y comercio**:
   - Vacíos o nulos

6. **Detalles de pago**:
   - Nulos o faltantes

7. **JSON malformado**:
   - Sintaxis incorrecta
   - Tipos de datos incorrectos

8. **Content-Type incorrecto** (HTTP 415):
   - `text/plain` en lugar de `application/json`

#### **✅ Casos que DEBEN ser aceptados (HTTP 200)**

1. **Pagos con tarjeta de crédito válidos**
2. **Pagos con PayPal válidos**
3. **Requests sin Content-Type** (Spring Boot lo infiere)

### 🔧 Configuración de Validación

Para habilitar las validaciones, asegúrate de:

1. **Usar `@Valid` en el controlador**:
```java
public ResponseEntity<Payment> processPayment(
    @Valid @RequestBody PaymentRequestDTO paymentRequestDTO
) {
    // ...
}
```

2. **Incluir dependencias de validación**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

3. **Configurar el `@ControllerAdvice`**:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    // Manejo de todas las excepciones
}
```

### 🧪 Tests de Validación

Los tests E2E cubren todos los escenarios de validación:

```typescript
test('debería rechazar pago con datos inválidos', async ({ request }) => {
  const invalidPaymentData = PaymentRequestFactory.createInvalidPayment();
  
  const response = await request.post('/api/v1/payments', {
    data: invalidPaymentData,
    headers: ApiSetup.getCommonHeaders()
  });
  
  expect(response.status()).toBe(400);
  const responseBody = await response.json();
  expect(responseBody.error).toBe('Bad Request');
});
```

### 📊 Métricas de Validación

- ✅ **12/12 tests E2E pasando**
- ✅ **100% cobertura de casos de error**
- ✅ **Respuestas HTTP consistentes**
- ✅ **Mensajes de error descriptivos**

## 🏗️ Arquitectura

El proyecto sigue los principios de **Arquitectura Hexagonal (Ports & Adapters)**:

```
src/main/java/com/paymentgateway/
├── application/          # Casos de uso y puertos
│   ├── port/
│   │   ├── in/          # Puertos de entrada
│   │   └── out/         # Puertos de salida
│   └── usecase/         # Implementación de casos de uso
├── domain/              # Lógica de negocio
│   ├── model/           # Entidades y objetos de valor
│   ├── repository/      # Interfaces de repositorio
│   └── service/         # Servicios de dominio
├── infrastructure/      # Adaptadores externos
│   ├── adapter/         # Implementaciones de puertos
│   ├── config/          # Configuración
│   ├── persistence/     # Repositorios JPA
│   ├── web/            # Controllers REST
│   │   ├── controller/  # Controladores
│   │   └── validation/  # Validaciones personalizadas
│   └── ...
└── shared/             # Utilidades compartidas
    └── exception/      # Excepciones personalizadas
```

## 🚀 CI/CD Pipeline

El proyecto incluye un pipeline automatizado de CI/CD que se ejecuta en cada Pull Request hacia `develop`:

### 🔄 Flujo del Pipeline

1. **🔍 Code Quality & Security**
   - Validación de formato de código
   - Análisis estático con SpotBugs
   - Escaneo con SonarCloud (opcional)

2. **🏗️ Build & Test**
   - Compilación del proyecto
   - Tests unitarios, integración y arquitectura
   - Tests E2E con Playwright
   - Generación de reportes de cobertura
   - Creación de artefactos JAR

3. **🔒 Security Scan**
   - OWASP Dependency Check
   - Escaneo de vulnerabilidades con Snyk

4. **🐳 Docker Build**
   - Construcción de imagen Docker
   - Validación de contenedor

5. **💬 Automated PR Comments**
   - Comentarios automáticos con estado del pipeline
   - Notificaciones de revisión

### 📋 Requisitos para Merge

- ✅ Todos los checks del CI/CD deben pasar
- ✅ Aprobación del code owner (propietario del repositorio)
- ✅ Resolución de todos los comentarios
- ✅ Branch actualizada con develop

## 🤝 Contribución

### 🔀 Proceso de Pull Request

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear un Pull Request hacia `develop`
6. **El CI/CD se ejecutará automáticamente**
7. **Solo el propietario puede aprobar y hacer merge**

### ✅ Checklist antes de crear PR

- [ ] Código sigue las convenciones del proyecto
- [ ] Tests añadidos y pasando localmente
- [ ] Tests E2E actualizados si es necesario
- [ ] Validaciones implementadas para nuevos campos
- [ ] Manejo de errores apropiado
- [ ] Documentación actualizada
- [ ] Sin warnings de linter
- [ ] Cobertura de tests mantenida

### 📊 Estándares de Código

- Seguir principios SOLID
- Mantener cobertura de tests > 70%
- Usar Lombok para reducir boilerplate
- Documentar APIs con OpenAPI/Swagger
- **Validar toda entrada de datos**
- **Manejar errores apropiadamente con códigos HTTP correctos**
- Implementar validaciones tanto en DTO como en dominio
- Crear tests para todos los casos de error

## 📝 Notas Adicionales

### Monitoreo y Observabilidad

- **Health Check**: `http://localhost:8080/actuator/health`
- **Métricas**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`

### Logs

Los logs se almacenan en:
- **Consola**: Formato simplificado
- **Archivo**: `logs/payment-gateway.log`

### Seguridad

- Autenticación JWT
- **Validación completa de entrada**
- **Manejo seguro de errores sin exposición de información sensible**
- Cifrado de datos sensibles
- Rate limiting

---

**¿Necesitas ayuda?** 🆘

Si tienes problemas con la configuración o ejecución del proyecto, revisa:
1. Que Docker esté ejecutándose
2. Las variables de entorno estén configuradas
3. Los puertos 8080, 5432 y 6379 estén disponibles
4. Las credenciales de Stripe y PayPal sean válidas(para ejecutar el proyecto no son necesarios)
5. **Que todos los tests E2E pasen antes de hacer cambios**

**Happy Coding! 🚀** 