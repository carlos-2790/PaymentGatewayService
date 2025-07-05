#!/bin/bash

# 🧪 Script para ejecutar tests E2E de API
# Uso: ./scripts/run-api-tests.sh [environment]

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
ENVIRONMENT=${1:-"local"}
API_BASE_URL="http://localhost:8080"
SPRING_PROFILE="test"

echo -e "${BLUE}🧪 Iniciando tests E2E de API${NC}"
echo -e "${BLUE}Environment: ${ENVIRONMENT}${NC}"
echo -e "${BLUE}API Base URL: ${API_BASE_URL}${NC}"

# Función para limpiar procesos al salir
cleanup() {
    echo -e "\n${YELLOW}🧹 Limpiando procesos...${NC}"
    if [ -f app.pid ]; then
        echo -e "${YELLOW}🛑 Deteniendo aplicación Spring Boot...${NC}"
        kill $(cat app.pid) 2>/dev/null || true
        rm app.pid
    fi
    
    # Detener Docker Compose si está ejecutándose
    if docker-compose ps | grep -q "Up"; then
        echo -e "${YELLOW}🐳 Deteniendo servicios Docker...${NC}"
        docker-compose down
    fi
}

# Configurar trap para cleanup
trap cleanup EXIT INT TERM

# Verificar dependencias
check_dependencies() {
    echo -e "${BLUE}🔍 Verificando dependencias...${NC}"
    
    if ! command -v java &> /dev/null; then
        echo -e "${RED}❌ Java no está instalado${NC}"
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}❌ Maven no está instalado${NC}"
        exit 1
    fi
    
    if ! command -v node &> /dev/null; then
        echo -e "${RED}❌ Node.js no está instalado${NC}"
        exit 1
    fi
    
    if ! command -v npx &> /dev/null; then
        echo -e "${RED}❌ NPX no está instalado${NC}"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}❌ Docker Compose no está instalado${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Todas las dependencias están instaladas${NC}"
}

# Iniciar servicios de infraestructura
start_infrastructure() {
    echo -e "${BLUE}🐳 Iniciando servicios de infraestructura...${NC}"
    docker-compose up -d postgres redis
    
    # Esperar a que los servicios estén listos
    echo -e "${YELLOW}⏳ Esperando a que PostgreSQL esté listo...${NC}"
    timeout 60s bash -c 'until docker-compose exec -T postgres pg_isready -U payment_user; do sleep 2; done'
    
    echo -e "${YELLOW}⏳ Esperando a que Redis esté listo...${NC}"
    timeout 60s bash -c 'until docker-compose exec -T redis redis-cli ping; do sleep 2; done'
    
    echo -e "${GREEN}✅ Servicios de infraestructura iniciados${NC}"
}

# Compilar aplicación
compile_application() {
    echo -e "${BLUE}🔧 Compilando aplicación Spring Boot...${NC}"
    mvn clean compile -DskipTests -q
    echo -e "${GREEN}✅ Aplicación compilada${NC}"
}

# Instalar dependencias de Node.js
install_node_dependencies() {
    echo -e "${BLUE}📦 Instalando dependencias de Node.js...${NC}"
    npm ci --silent
    echo -e "${GREEN}✅ Dependencias de Node.js instaladas${NC}"
}

# Instalar Playwright
install_playwright() {
    echo -e "${BLUE}🎭 Instalando Playwright browsers...${NC}"
    npx playwright install --with-deps
    echo -e "${GREEN}✅ Playwright instalado${NC}"
}

# Iniciar aplicación Spring Boot
start_application() {
    echo -e "${BLUE}🚀 Iniciando aplicación Spring Boot...${NC}"
    
    mvn spring-boot:run \
        -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=${SPRING_PROFILE}" \
        -Dspring.datasource.url=jdbc:postgresql://localhost:5432/payment_gateway \
        -Dspring.datasource.username=payment_user \
        -Dspring.datasource.password=payment_password \
        -Dspring.data.redis.host=localhost \
        -Dspring.data.redis.port=6379 &
    
    echo $! > app.pid
    
    # Esperar a que la aplicación esté lista
    echo -e "${YELLOW}⏳ Esperando a que la aplicación esté lista...${NC}"
    timeout 120s bash -c "until curl -f ${API_BASE_URL}/api/v1/payments/health &>/dev/null; do sleep 2; done"
    
    if curl -f ${API_BASE_URL}/api/v1/payments/health &>/dev/null; then
        echo -e "${GREEN}✅ Aplicación iniciada correctamente${NC}"
    else
        echo -e "${RED}❌ La aplicación no pudo iniciarse${NC}"
        exit 1
    fi
}

# Ejecutar tests E2E
run_tests() {
    echo -e "${BLUE}🧪 Ejecutando tests E2E de API...${NC}"
    
    export API_BASE_URL="${API_BASE_URL}"
    export CI=true
    
    if npx playwright test tests/api/ --reporter=html,line; then
        echo -e "${GREEN}✅ Todos los tests E2E pasaron correctamente${NC}"
        return 0
    else
        echo -e "${RED}❌ Algunos tests E2E fallaron${NC}"
        return 1
    fi
}

# Mostrar reporte
show_report() {
    if [ -f "playwright-report/index.html" ]; then
        echo -e "${BLUE}📊 Reporte HTML generado en: playwright-report/index.html${NC}"
        echo -e "${BLUE}Para ver el reporte ejecuta: npx playwright show-report${NC}"
    fi
}

# Función principal
main() {
    echo -e "${GREEN}🎯 Payment Gateway Service - Tests E2E de API${NC}"
    echo -e "${GREEN}=================================================${NC}"
    
    check_dependencies
    start_infrastructure
    compile_application
    install_node_dependencies
    install_playwright
    start_application
    
    if run_tests; then
        echo -e "\n${GREEN}🎉 ¡Todos los tests E2E pasaron correctamente!${NC}"
        show_report
        exit 0
    else
        echo -e "\n${RED}💥 Algunos tests E2E fallaron${NC}"
        show_report
        exit 1
    fi
}

# Ejecutar función principal
main "$@" 