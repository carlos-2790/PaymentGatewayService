@echo off
echo ========================================
echo PRUEBA DE CONFIGURACION CI/CD
echo ========================================
echo.

echo 🧪 Probando configuracion de tests sin servidor...
echo.

echo 1. Tests unitarios (perfil unit):
mvn test -Dspring.profiles.active=unit -Dspring.main.web-application-type=none -Dtest="!**/*ApiTest,!**/*IntegrationTest,!**/*E2ETest" -q --no-transfer-progress

echo.
echo 2. Tests de API (perfil ci):
mvn test -Dspring.profiles.active=ci -Dspring.main.web-application-type=none -Dtest="**/*ApiTest,**/*ControllerTest" -q --no-transfer-progress

echo.
echo 3. Verificando estructura de workflows:
if exist ".github\workflows\tests-only.yml" (
    echo ✅ Workflow de tests configurado
) else (
    echo ❌ Workflow de tests NO encontrado
)

if exist ".github\workflows\branch-protection.yml" (
    echo ✅ Workflow de protección de ramas configurado
) else (
    echo ❌ Workflow de protección NO encontrado
)

if exist ".github\CODEOWNERS" (
    echo ✅ CODEOWNERS configurado
) else (
    echo ❌ CODEOWNERS NO encontrado
)

echo.
echo 4. Verificando configuraciones de test:
if exist "src\test\resources\application-unit.yml" (
    echo ✅ Configuración de tests unitarios
) else (
    echo ❌ Configuración de tests unitarios NO encontrada
)

if exist "src\test\resources\application-ci.yml" (
    echo ✅ Configuración de tests CI/CD
) else (
    echo ❌ Configuración de tests CI/CD NO encontrada
)

echo.
echo ========================================
echo CONFIGURACION CI/CD COMPLETADA
echo ========================================
echo.
echo 📋 Próximos pasos:
echo 1. Actualizar tu username en los workflows
echo 2. Configurar branch protection en GitHub
echo 3. Probar con un PR de prueba
echo.
pause 