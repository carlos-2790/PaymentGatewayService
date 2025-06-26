# 🔒 Configuración de Protección de Branches

Este documento describe cómo configurar las reglas de protección de branches para que solo tú puedas aprobar y hacer merge de PRs hacia `develop`.

## 📋 Pasos para configurar la protección de branches

### 1. Acceder a la configuración del repositorio

1. Ve a tu repositorio en GitHub
2. Haz clic en **Settings** (Configuración)
3. En el menú lateral, haz clic en **Branches**

### 2. Crear regla de protección para `develop`

1. Haz clic en **Add rule** (Agregar regla)
2. En **Branch name pattern**, escribe: `develop`

### 3. Configurar las reglas de protección

Marca las siguientes opciones:

#### ✅ **Restrict pushes that create files larger than 100MB**
- Evita archivos grandes en el repositorio

#### ✅ **Require a pull request before merging**
- **Require approvals**: `1` (mínimo 1 aprobación)
- ✅ **Dismiss stale reviews when new commits are pushed**
- ✅ **Require review from code owners** (esto hará que solo tú puedas aprobar)
- ✅ **Restrict reviews to code owners** (solo code owners pueden aprobar)
- ✅ **Require approval of the most recent reviewable push**

#### ✅ **Require status checks to pass before merging**
- ✅ **Require branches to be up to date before merging**
- En **Search for status checks**, busca y selecciona:
  - `Code Quality & Security`
  - `Build & Test`
  - `Security Scan`
  - `Docker Build`

#### ✅ **Require conversation resolution before merging**
- Todos los comentarios deben resolverse antes del merge

#### ✅ **Require signed commits**
- Opcional: Para mayor seguridad

#### ✅ **Require linear history**
- Mantiene el historial limpio

#### ✅ **Require deployments to succeed before merging**
- Opcional: Si tienes environments configurados

#### ✅ **Lock branch**
- ❌ **NO marcar** - Esto bloquearía completamente la branch

#### ✅ **Do not allow bypassing the above settings**
- ✅ **Restrict pushes that create files larger than 100MB**

#### ✅ **Restrict who can push to matching branches**
- En **Restrict pushes**, selecciona:
  - ✅ **Restrict pushes that create files larger than 100MB**
  - En **People and teams with push access**, añádete solo a ti

### 4. Configurar regla similar para `main` (opcional)

Repite los mismos pasos para la branch `main` si quieres protegerla también.

## 🔧 Configuración adicional recomendada

### Configurar Environments (Opcional)

1. Ve a **Settings** → **Environments**
2. Crea un environment llamado `production`
3. Configura **Deployment protection rules**:
   - ✅ **Required reviewers**: Añádete a ti mismo
   - ✅ **Wait timer**: 0 minutos (o el tiempo que desees)

### Configurar Secrets necesarios

Ve a **Settings** → **Secrets and variables** → **Actions** y añade:

```bash
# Para SonarCloud (opcional)
SONAR_TOKEN=tu_sonar_token

# Para Snyk (opcional)
SNYK_TOKEN=tu_snyk_token

# Para Codecov (opcional)
CODECOV_TOKEN=tu_codecov_token
```

## 📊 Resultado esperado

Con esta configuración:

1. ✅ **Solo tú puedes aprobar PRs** hacia `develop`
2. ✅ **El CI/CD se ejecuta automáticamente** en cada PR
3. ✅ **Todos los checks deben pasar** antes de permitir merge
4. ✅ **Los comentarios deben resolverse** antes del merge
5. ✅ **El historial se mantiene limpio** (linear history)
6. ✅ **Solo tú puedes hacer push directo** a `develop`

## 🚀 Flujo de trabajo resultante

1. **Desarrollador crea PR** hacia `develop`
2. **CI/CD se ejecuta automáticamente**:
   - Code Quality & Security
   - Build & Test
   - Security Scan
   - Docker Build
3. **Bot comenta el estado** del pipeline en el PR
4. **Solo tú recibes notificación** para review
5. **Solo tú puedes aprobar** el PR
6. **Merge solo es posible** cuando:
   - Tienes tu aprobación
   - Todos los checks pasan
   - Comentarios están resueltos

## 🛠️ Comandos útiles para ti como owner

```bash
# Forzar push (solo en emergencias)
git push --force-with-lease origin develop

# Bypass protections (solo owners)
# Se hace desde la interfaz de GitHub con permisos de admin
```

## ⚠️ Notas importantes

- **CODEOWNERS**: El archivo `.github/CODEOWNERS` ya está configurado para que seas el único reviewer requerido
- **Permisos**: Asegúrate de tener permisos de **Admin** en el repositorio
- **Tokens**: Los tokens de terceros (SonarCloud, Snyk) son opcionales pero recomendados
- **Notifications**: Configurar notificaciones de GitHub para recibir alerts de PRs

---

**🎯 Una vez configurado, tendrás control total sobre qué código se integra a `develop`, manteniendo la calidad y seguridad del proyecto.** 