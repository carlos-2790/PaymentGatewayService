#!/bin/bash

echo "========================================="
echo "CI/CD - EJECUTANDO TESTS SIN SERVIDOR"
echo "========================================="
echo ""

# Configurar variables de entorno para CI/CD
export SPRING_PROFILES_ACTIVE=ci
export SPRING_MAIN_WEB_APPLICATION_TYPE=none
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"

echo "Configuración CI/CD:"
echo "- Perfil: $SPRING_PROFILES_ACTIVE"
echo "- Tipo de aplicación: $SPRING_MAIN_WEB_APPLICATION_TYPE"
echo "- Memoria Maven: $MAVEN_OPTS"
echo ""

# Ejecutar tests sin servidor
echo "Ejecutando tests..."
mvn clean test \
  -Dspring.profiles.active=ci \
  -Dspring.main.web-application-type=none \
  -Dmaven.test.failure.ignore=false \
  -Dmaven.test.skip=false \
  -B \
  -q

# Verificar resultado
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ TESTS COMPLETADOS EXITOSAMENTE"
    echo "========================================="
    exit 0
else
    echo ""
    echo "❌ TESTS FALLARON"
    echo "========================================="
    exit 1
fi 