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

## 🛠️ Tecnologías

- **Java 21**
- **Spring Boot 3.2.1**
- **Spring Security**
- **Spring Data JPA**
- **PostgreSQL 14**
- **Redis 7**
- **Maven**
- **Docker & Docker Compose**
- **Flyway**
- **Swagger/OpenAPI**
- **Lombok**
- **MapStruct**

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
| `POST` | `/api/v1/payments/process` | Procesar un pago |
| `GET` | `/api/v1/payments/{id}` | Consultar estado de pago |
| `GET` | `/api/v1/payments` | Listar pagos |

### Ejemplo de Request

```json
{
  "amount": 100.00,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "gateway": "STRIPE",
  "paymentDetails": {
    "cardNumber": "4242424242424242",
    "expiryMonth": "12",
    "expiryYear": "2025",
    "cvv": "123"
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
```

### Tests Disponibles

- ✅ **Tests Unitarios**: Lógica de dominio y casos de uso
- ✅ **Tests de Integración**: Controllers y repositorios
- ✅ **Tests de Arquitectura**: Validación de principios hexagonales
- ✅ **TestContainers**: Tests con base de datos real

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
│   └── web/            # Controllers REST
└── shared/             # Utilidades compartidas
```

## 🤝 Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear un Pull Request

### Estándares de Código

- Seguir principios SOLID
- Mantener cobertura de tests > 80%
- Usar Lombok para reducir boilerplate
- Documentar APIs con OpenAPI/Swagger

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
- Validación de entrada
- Cifrado de datos sensibles
- Rate limiting

---

**¿Necesitas ayuda?** 🆘

Si tienes problemas con la configuración o ejecución del proyecto, revisa:
1. Que Docker esté ejecutándose
2. Las variables de entorno estén configuradas
3. Los puertos 8080, 5432 y 6379 estén disponibles
4. Las credenciales de Stripe y PayPal sean válidas(para ejecutar el proyecto no son necesarios)

**Happy Coding! 🚀** 