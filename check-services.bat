@echo off
echo ========================================
echo DIAGNOSTICO DE SERVICIOS REQUERIDOS
echo ========================================
echo.

echo Verificando Java...
java -version
echo.

echo Verificando Maven...
mvn -version
echo.

echo Verificando puerto 8080...
netstat -an | findstr :8080
if %errorlevel% == 0 (
    echo ADVERTENCIA: Puerto 8080 ya esta en uso
) else (
    echo Puerto 8080 disponible
)
echo.

echo Verificando PostgreSQL (puerto 5432)...
netstat -an | findstr :5432
if %errorlevel% == 0 (
    echo PostgreSQL parece estar ejecutandose
) else (
    echo PostgreSQL no detectado
)
echo.

echo Verificando Redis (puerto 6379)...
netstat -an | findstr :6379
if %errorlevel% == 0 (
    echo Redis parece estar ejecutandose
) else (
    echo Redis no detectado
)
echo.

echo ========================================
echo RECOMENDACIONES:
echo ========================================
echo 1. Para desarrollo local, usa: run-local.bat
echo 2. Para produccion, asegurate de que PostgreSQL y Redis esten ejecutandose
echo 3. Para Docker, usa: docker-compose up
echo.
pause 