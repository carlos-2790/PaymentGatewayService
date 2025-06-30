# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar archivos de configuración primero para aprovechar el cache de Docker
COPY pom.xml .
COPY package.json .

# Descargar dependencias (se cachea si no cambian los archivos de configuración)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar y empaquetar la aplicación
RUN mvn clean package -DskipTests -B

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Crear usuario no-root para seguridad
RUN addgroup -S paymentgateway && adduser -S paymentgateway -G paymentgateway

# Instalar herramientas necesarias
RUN apk add --no-cache curl

# Crear directorios necesarios
RUN mkdir -p /app/logs && \
    chown -R paymentgateway:paymentgateway /app

WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=builder /app/target/payment-gateway-service-*.jar app.jar

# Cambiar al usuario no-root
USER paymentgateway

# Configurar JVM para contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 