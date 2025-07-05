# 🚀 Comandos Disponibles - Payment Gateway Service

## 📋 Comandos Principales

### 🏁 Inicio Rápido
```bash
# Configuración inicial (solo la primera vez)
npm run setup

# Desarrollo completo: Docker + Spring Boot
npm run dev

# Desarrollo con limpieza completa
npm run dev:clean
```

### 🏃‍♂️ Comandos de Desarrollo

| Comando | Descripción |
|---------|-------------|
| `npm run dev` | 🔥 **Inicia Docker + Spring Boot** (Comando principal) |
| `npm run dev:clean` | 🧹 Limpia todo + Docker + Spring Boot |
| `npm run start` | ⚡ Solo Spring Boot (requiere Docker corriendo) |
| `npm run start:dev` | 🐳 Docker + espera + Spring Boot |

### 🐳 Comandos Docker

| Comando | Descripción |
|---------|-------------|
| `npm run docker:up` | 🚀 Inicia PostgreSQL + Redis |
| `npm run docker:down` | 🛑 Detiene todos los contenedores |

### 🧪 Comandos de Testing

| Comando | Descripción |
|---------|-------------|
| `npm run test` | 🔧 Tests unitarios (Maven) |
| `npm run test:integration` | 🔗 Tests de integración con Docker |
| `npm run test:api` | 🌐 Tests E2E con Playwright |
| `npm run test:api:headed` | 👀 Tests E2E con navegador visible |
| `npm run test:api:debug` | 🐛 Tests E2E en modo debug |
| `npm run test:api:report` | 📊 Ver reporte de tests E2E |

### 🚀 Pipeline Completo

| Comando | Descripción |
|---------|-------------|
| `npm run test:full` | 🎯 **Pipeline completo** (Maven + Docker + Spring Boot + Tests E2E) |
| `npm run test:full:jar` | 📦 Pipeline con JAR |
| `npm run test:full:docker` | 🐳 Pipeline con Docker |

### 🛠️ Comandos de Utilidad

| Comando | Descripción |
|---------|-------------|
| `npm run setup` | 📥 Instalación inicial (npm + playwright) |
| `npm run clean` | 🧹 Limpieza completa (Maven + Docker) |
| `npm run format` | 💅 Formatear código Java |
| `npm run format:all` | ✨ Formatear todo el código |

---

## 🎯 Flujo de Trabajo Recomendado

### Para Desarrollo Diario:
```bash
npm run dev
```

### Para Testing Completo:
```bash
npm run test:full
```

### Para Configuración Inicial:
```bash
npm run setup
npm run dev
```

### Para Limpieza Completa:
```bash
npm run clean
npm run dev:clean
```

---

## 📝 Notas Importantes

- ✅ **`npm run dev`** es el comando principal para desarrollo
- ✅ **`npm run test:full`** ejecuta el pipeline completo
- ✅ Los comandos manejan Docker automáticamente
- ✅ Spring Boot se inicia en puerto 8080
- ✅ PostgreSQL en puerto 5432
- ✅ Redis en puerto 6379

---

## 🔧 Solución de Problemas

Si tienes problemas:

1. **Limpieza completa**: `npm run clean`
2. **Reinicio completo**: `npm run dev:clean`
3. **Verificar puertos**: Asegúrate que 8080, 5432, 6379 estén libres
4. **Logs**: Revisa `spring-boot-startup.log` y `spring-boot-error.log` 