@echo off
echo ========================================
echo TESTS RAPIDOS (SIN SERVIDOR)
echo ========================================
echo.

echo Ejecutando tests sin recompilacion completa...
mvn test -Dspring.profiles.active=ci -Dspring.main.web-application-type=none -Dmaven.test.skip=false

echo.
echo Tests completados rapidamente.
pause 