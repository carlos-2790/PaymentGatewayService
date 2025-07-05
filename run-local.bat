@echo off
echo Iniciando Payment Gateway Service en modo local...
echo.

echo Verificando puerto 8080...
netstat -ano | findstr :8080 | findstr LISTENING
if %errorlevel% == 0 (
    echo ADVERTENCIA: Puerto 8080 ya esta en uso
    echo Terminando procesos en puerto 8080...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do taskkill /PID %%a /F
    timeout /t 2 /nobreak >nul
) else (
    echo Puerto 8080 disponible
)

echo.
echo Configuracion:
echo - Base de datos: H2 en memoria
echo - Puerto: 8080
echo - Perfil: local
echo - Consola H2: http://localhost:8080/h2-console
echo.
mvn spring-boot:run -Dspring-boot.run.profiles=local
pause 