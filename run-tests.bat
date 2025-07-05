@echo off
echo ========================================
echo 🚀 PAYMENT GATEWAY - SCRIPT COMPLETO
echo ========================================
echo.
echo Este script hará:
echo 1. Levantar Docker (PostgreSQL + Redis)
echo 2. Compilar y levantar Spring Boot
echo 3. Ejecutar tests E2E completos
echo 4. Cleanup total (matar procesos y liberar puertos)
echo.
echo Presiona CTRL+C para cancelar en cualquier momento
echo.
pause

powershell -ExecutionPolicy Bypass -File .\scripts\run-api-tests-final.ps1

pause