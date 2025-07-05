@echo off
echo ========================================
echo EJECUTANDO TESTS SIN SERVIDOR SPRING
echo ========================================
echo.

echo Configuracion para CI/CD:
echo - Tests unitarios: SI
echo - Tests de integracion: SI
echo - Servidor Spring Boot: NO
echo - Base de datos: H2 en memoria
echo - Servicios externos: DESHABILITADOS
echo.

echo Ejecutando tests...
mvn clean test -Dspring.profiles.active=ci -Dspring.main.web-application-type=none

echo.
echo ========================================
echo TESTS COMPLETADOS
echo ========================================
pause 