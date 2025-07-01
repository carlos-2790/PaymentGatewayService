# 🚀 Configuración GitHub Actions y Docker

## 🔄 GitHub Actions

### Workflows Configurados

#### 1. **CI/CD Pipeline** (`ci-cd.yml`)
**Ejecuta en:** PRs a `develop` y push a `develop`/`main`

**Validaciones:**
- ✅ **Calidad de Código:** SpotBugs, SonarCloud
- ✅ **Build & Test:** Compilación, tests unitarios/integración/arquitectura
- ✅ **Seguridad:** OWASP Dependency Check, Snyk
- ✅ **Docker:** Construcción de imagen

**Servicios de Test:**
- PostgreSQL 14
- Redis 7

#### 2. **PR Validation** (`pr-validation.yml`)
**Ejecuta en:** PRs a `main`/`master`

**Validaciones:**
- ✅ Tests unitarios
- ✅ Compilación
- 💬 Comentario automático con resultados

### Configuración Adicional
- **CODEOWNERS:** Define revisores automáticos
- **Branch Protection:** Reglas de protección de ramas
- **PR Template:** Plantilla para pull requests

---

## 🐳 Docker

### Archivos de Configuración

#### **Dockerfile**
- 🏗️ **Multi-stage build** (Maven + JRE Alpine)
- 🔒 **Usuario no-root** para seguridad
- 🏥 **Health check** en `/actuator/health`
- ⚡ **Optimizado** para contenedores con JVM flags

#### **docker-compose.yml**
**Servicios:**
- 🐘 **PostgreSQL 15:** Base de datos principal
- 🔴 **Redis 7:** Cache y sesiones
- 💾 **Volúmenes persistentes** para datos

#### **Dockerfile.alternative**
- 🔄 Configuración alternativa de Docker

---

## 🔧 Uso Rápido

### Desarrollo Local
```bash
# Levantar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f
```

### Build Manual
```bash
# Construir imagen
docker build -t payment-gateway .

# Ejecutar contenedor
docker run -p 8080:8080 payment-gateway
```

---

## 📋 Variables de Entorno

**GitHub Secrets requeridos:**
- `SONAR_TOKEN` - Token de SonarCloud
- `SNYK_TOKEN` - Token de Snyk Security

**Docker Environment:**
- Base de datos y Redis configurados automáticamente
- JVM optimizada para contenedores 