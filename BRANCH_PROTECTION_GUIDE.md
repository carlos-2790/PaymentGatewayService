# 🛡️ Guía de Protección de Ramas

## Ramas Protegidas

Las siguientes ramas están protegidas y **NO permiten push directo**:
- `main` - Rama de producción
- `develop` - Rama de desarrollo

## 🔄 Flujo de Trabajo Requerido

### 1. Crear una rama de feature
```bash
# Desde develop
git checkout develop
git pull origin develop
git checkout -b feature/tu-nueva-funcionalidad
```

### 2. Desarrollar y hacer commits
```bash
git add .
git commit -m "feat: descripción de tu cambio"
git push origin feature/tu-nueva-funcionalidad
```

### 3. Crear Pull Request
1. Ve a GitHub
2. Crea un Pull Request hacia `develop`
3. Completa la descripción del PR
4. Espera la aprobación y que pasen todos los checks

### 4. Merge automático
Una vez aprobado, el PR se puede mergear automáticamente.

## ✅ Validaciones Automáticas

Cada PR debe pasar:
- ✅ Tests unitarios
- ✅ Tests de integración
- ✅ Tests de API E2E
- ✅ Análisis de seguridad
- ✅ Cobertura de código mínima (70%)
- ✅ Revisión de código (1 aprobación mínima)

## 🚫 Qué NO hacer

```bash
# ❌ Esto fallará
git push origin main
git push origin develop
```

## 📋 Checklist antes de crear PR

- [ ] Mi código compila sin errores
- [ ] Agregué tests para nueva funcionalidad
- [ ] Los tests pasan localmente
- [ ] Actualicé documentación si es necesario
- [ ] El commit sigue las convenciones
- [ ] No hay secretos o datos sensibles

## 🆘 En caso de emergencia

Solo los administradores pueden hacer bypass de estas reglas en casos excepcionales.

## 📞 Contacto

Si tienes dudas, contacta al administrador del repositorio. 