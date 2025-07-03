# Script FINAL: Docker + Spring Boot + Tests E2E
# Uso: .\scripts\run-api-tests-final.ps1 [-StartupMethod "maven"]

param(
    [ValidateSet("maven", "jar", "docker")]
    [string]$StartupMethod = "maven"
)

Write-Host "========================================" -ForegroundColor Green
Write-Host " PAYMENT GATEWAY - PIPELINE COMPLETO" -ForegroundColor Green  
Write-Host "========================================" -ForegroundColor Green
Write-Host "Metodo de inicio: $StartupMethod" -ForegroundColor Blue
Write-Host ""

# Variables globales
$API_BASE_URL = "http://localhost:8080"
$SPRING_PROFILE = "dev"
$APP_PID = $null
$DOCKER_STARTED = $false
$CONTAINER_NAME = "payment-gateway-test"

# Funcion de cleanup completo
function Complete-Cleanup {
    Write-Host ""
    Write-Host "Iniciando cleanup..." -ForegroundColor Yellow
    
    # Detener Spring Boot
    if ($APP_PID) {
        Write-Host "   Deteniendo Spring Boot (PID: $APP_PID)..." -ForegroundColor Yellow
        try {
            Stop-Process -Id $APP_PID -Force -ErrorAction SilentlyContinue
            Write-Host "   Spring Boot detenido" -ForegroundColor Green
        } catch {
            Write-Host "   Proceso ya terminado" -ForegroundColor Yellow
        }
    }
    
    # Detener contenedor Docker
    if ($StartupMethod -eq "docker") {
        Write-Host "   Deteniendo contenedor Docker..." -ForegroundColor Yellow
        try {
            docker stop $CONTAINER_NAME 2>$null
            docker rm $CONTAINER_NAME 2>$null
            Write-Host "   Contenedor detenido" -ForegroundColor Green
        } catch {
            Write-Host "   No hay contenedor ejecutandose" -ForegroundColor Yellow
        }
    }
    
    # Liberar puerto 8080
    Write-Host "   Liberando puerto 8080..." -ForegroundColor Yellow
    $port8080 = netstat -ano | findstr ":8080" | findstr "LISTENING"
    if ($port8080) {
        $processIds = $port8080 | ForEach-Object { ($_ -split '\s+')[-1] } | Sort-Object -Unique
        foreach ($processId in $processIds) {
            if ($processId -and $processId -ne "0") {
                taskkill /PID $processId /F 2>$null
            }
        }
    }
    
    # Detener Docker Compose
    if ($DOCKER_STARTED) {
        Write-Host "   Deteniendo Docker Compose..." -ForegroundColor Yellow
        docker-compose down 2>$null
        Write-Host "   Docker Compose detenido" -ForegroundColor Green
    }
    
    # Limpiar archivos temporales
    @("spring-boot-startup.log", "spring-boot-error.log") | ForEach-Object {
        if (Test-Path $_) {
            Remove-Item $_ -Force
        }
    }
    
    Write-Host "Cleanup completado" -ForegroundColor Green
}

# Configurar trap para cleanup automatico
$ErrorActionPreference = "Continue"
trap { Complete-Cleanup; exit 1 }

# PASO 1: Verificar dependencias
function Test-Dependencies {
    Write-Host "1. Verificando dependencias..." -ForegroundColor Blue
    
    $dependencies = @("java", "mvn", "node", "npx", "docker", "docker-compose")
    $allOk = $true
    
    foreach ($dep in $dependencies) {
        if (Get-Command $dep -ErrorAction SilentlyContinue) {
            Write-Host "   $dep OK" -ForegroundColor Green
        } else {
            Write-Host "   $dep NO encontrado" -ForegroundColor Red
            $allOk = $false
        }
    }
    
    if (-not $allOk) {
        Write-Host "Faltan dependencias requeridas" -ForegroundColor Red
        exit 1
    }
    
    Write-Host ""
}

# PASO 2: Cleanup inicial
function Initial-Cleanup {
    Write-Host "2. Cleanup inicial..." -ForegroundColor Blue
    
    # Liberar puerto 8080
    $port8080 = netstat -ano | findstr ":8080" | findstr "LISTENING"
    if ($port8080) {
        Write-Host "   Liberando puerto 8080..." -ForegroundColor Yellow
        $processIds = $port8080 | ForEach-Object { ($_ -split '\s+')[-1] } | Sort-Object -Unique
        foreach ($processId in $processIds) {
            if ($processId -and $processId -ne "0") {
                taskkill /PID $processId /F 2>$null
            }
        }
    }
    
    Write-Host "   Puerto 8080 libre" -ForegroundColor Green
    Write-Host ""
}

# PASO 3: Iniciar Docker
function Start-Docker {
    Write-Host "3. Iniciando servicios Docker..." -ForegroundColor Blue
    
    docker-compose up -d postgres redis
    if ($LASTEXITCODE -ne 0) {
        Write-Host "   Error iniciando Docker" -ForegroundColor Red
        exit 1
    }
    
    $script:DOCKER_STARTED = $true
    
    # Esperar PostgreSQL
    Write-Host "   Esperando PostgreSQL..." -ForegroundColor Yellow
    $timeout = 60
    $elapsed = 0
    
    do {
        Start-Sleep -Seconds 2
        $elapsed += 2
        try {
            docker-compose exec -T postgres pg_isready -U payment_user 2>$null
            if ($LASTEXITCODE -eq 0) { 
                Write-Host "   PostgreSQL listo" -ForegroundColor Green
                break 
            }
        } catch { }
    } while ($elapsed -lt $timeout)
    
    if ($elapsed -ge $timeout) {
        Write-Host "   PostgreSQL no se inicio" -ForegroundColor Red
        exit 1
    }
    
    Write-Host ""
}

# PASO 4: Compilar aplicacion
function Build-Application {
    Write-Host "4. Compilando aplicacion..." -ForegroundColor Blue
    
    mvn clean compile -DskipTests -q
    if ($LASTEXITCODE -ne 0) {
        Write-Host "   Error compilando" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "   Aplicacion compilada" -ForegroundColor Green
    Write-Host ""
}

# PASO 5: Instalar dependencias Node.js
function Install-NodeDependencies {
    Write-Host "5. Verificando dependencias Node.js..." -ForegroundColor Blue
    
    if (-not (Test-Path "node_modules")) {
        Write-Host "   Instalando dependencias..." -ForegroundColor Yellow
        npm ci --silent
        if ($LASTEXITCODE -ne 0) {
            Write-Host "   Error instalando dependencias" -ForegroundColor Red
            exit 1
        }
    }
    
    Write-Host "   Dependencias Node.js OK" -ForegroundColor Green
    Write-Host ""
}

# PASO 6: Instalar Playwright
function Install-Playwright {
    Write-Host "6. Verificando Playwright..." -ForegroundColor Blue
    
    try {
        npx playwright --version 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   Playwright instalado" -ForegroundColor Green
        }
    } catch {
        Write-Host "   Instalando Playwright..." -ForegroundColor Yellow
        npx playwright install --with-deps
        if ($LASTEXITCODE -ne 0) {
            Write-Host "   Error instalando Playwright" -ForegroundColor Red
            exit 1
        }
    }
    
    Write-Host ""
}

# PASO 7: Iniciar Spring Boot segun metodo
function Start-SpringBoot-Maven {
    Write-Host "7. Iniciando Spring Boot con Maven..." -ForegroundColor Blue
    
    # Configurar variables de entorno
    $env:SPRING_PROFILES_ACTIVE = $SPRING_PROFILE
    $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/payment_gateway"
    $env:SPRING_DATASOURCE_USERNAME = "payment_user"
    $env:SPRING_DATASOURCE_PASSWORD = "payment_password"
    
    # Iniciar Spring Boot usando cmd para evitar problemas de redireccion
    $logFile = "spring-boot-startup.log"
    $errorFile = "spring-boot-error.log"
    
    # Usar cmd para manejar la redireccion correctamente
    $process = Start-Process -FilePath "cmd" -ArgumentList "/c", "mvn spring-boot:run > $logFile 2> $errorFile" -PassThru -WindowStyle Hidden
    $script:APP_PID = $process.Id
    
    Write-Host "   Spring Boot iniciando (PID: $APP_PID)" -ForegroundColor Yellow
    Write-Host "   Logs en: $logFile" -ForegroundColor Cyan
    Write-Host "   Errores en: $errorFile" -ForegroundColor Cyan
    
    Wait-SpringBootReady
}

function Start-SpringBoot-Jar {
    Write-Host "7. Iniciando Spring Boot con JAR..." -ForegroundColor Blue
    
    # Compilar JAR
    if (-not (Test-Path "target/*.jar")) {
        Write-Host "   Compilando JAR..." -ForegroundColor Yellow
        mvn clean package -DskipTests -q
        if ($LASTEXITCODE -ne 0) {
            Write-Host "   Error compilando JAR" -ForegroundColor Red
            exit 1
        }
    }
    
    # Encontrar JAR
    $jarFile = Get-ChildItem -Path "target" -Filter "*.jar" -Exclude "*sources*", "*javadoc*" | Select-Object -First 1
    if (-not $jarFile) {
        Write-Host "   No se encontro JAR" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "   Usando: $($jarFile.Name)" -ForegroundColor Cyan
    
    # Argumentos JVM
    $jvmArgs = @(
        "-Xmx1g",
        "-Dspring.profiles.active=$SPRING_PROFILE",
        "-Dspring.datasource.url=jdbc:postgresql://localhost:5432/payment_gateway",
        "-Dspring.datasource.username=payment_user",
        "-Dspring.datasource.password=payment_password",
        "-jar",
        $jarFile.FullName
    )
    
    # Iniciar
    $process = Start-Process -FilePath "java" -ArgumentList $jvmArgs -PassThru -WindowStyle Hidden
    $script:APP_PID = $process.Id
    
    Write-Host "   Spring Boot iniciando (PID: $APP_PID)" -ForegroundColor Yellow
    
    Wait-SpringBootReady
}

function Start-SpringBoot-Docker {
    Write-Host "7. Iniciando Spring Boot con Docker..." -ForegroundColor Blue
    
    # Construir imagen
    Write-Host "   Construyendo imagen..." -ForegroundColor Yellow
    docker build -t payment-gateway:test . --quiet
    if ($LASTEXITCODE -ne 0) {
        Write-Host "   Error construyendo imagen" -ForegroundColor Red
        exit 1
    }
    
    # Ejecutar contenedor
    Write-Host "   Iniciando contenedor..." -ForegroundColor Yellow
    docker run -d --name $CONTAINER_NAME `
        --network host `
        -e SPRING_PROFILES_ACTIVE=$SPRING_PROFILE `
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/payment_gateway `
        -e SPRING_DATASOURCE_USERNAME=payment_user `
        -e SPRING_DATASOURCE_PASSWORD=payment_password `
        payment-gateway:test
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "   Error iniciando contenedor" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "   Contenedor iniciado: $CONTAINER_NAME" -ForegroundColor Yellow
    
    Wait-SpringBootReady
}

# Funcion para esperar que Spring Boot este listo
function Wait-SpringBootReady {
    Write-Host "   Esperando que Spring Boot este listo..." -ForegroundColor Yellow
    
    $timeout = 180
    $elapsed = 0
    $ready = $false
    
    # Endpoints a verificar
    $endpoints = @("/api/v1/payments/health")
    
    do {
        Start-Sleep -Seconds 3
        $elapsed += 3
        
        foreach ($endpoint in $endpoints) {
            try {
                $response = Invoke-WebRequest -Uri "$API_BASE_URL$endpoint" -TimeoutSec 5 -ErrorAction SilentlyContinue
                if ($response.StatusCode -eq 200) {
                    Write-Host ""
                    Write-Host "   Endpoint $endpoint OK" -ForegroundColor Green
                    $ready = $true
                    break
                }
            } catch {
                # Continuar
            }
        }
        
        if ($ready) { break }
        
        Write-Host "." -NoNewline -ForegroundColor Yellow
        
        # Mostrar progreso cada 30 segundos
        if ($elapsed % 30 -eq 0) {
            Write-Host " ($elapsed/$timeout s)" -ForegroundColor Gray
        }
        
    } while ($elapsed -lt $timeout)
    
    Write-Host ""
    
    if ($ready) {
        Write-Host "   Spring Boot iniciado correctamente" -ForegroundColor Green
        Write-Host "   Disponible en: $API_BASE_URL" -ForegroundColor Cyan
        Write-Host "   Swagger: $API_BASE_URL/swagger-ui.html" -ForegroundColor Cyan
    } else {
        Write-Host "   Spring Boot no se inicio en $timeout segundos" -ForegroundColor Red
        
        # Mostrar logs si estan disponibles
        if (Test-Path "spring-boot-startup.log") {
            Write-Host "   Ultimas lineas del log:" -ForegroundColor Yellow
            Get-Content "spring-boot-startup.log" -Tail 10 | ForEach-Object { 
                Write-Host "      $_" -ForegroundColor Gray 
            }
        }
        
        if (Test-Path "spring-boot-error.log") {
            Write-Host "   Ultimas lineas de errores:" -ForegroundColor Yellow
            Get-Content "spring-boot-error.log" -Tail 10 | ForEach-Object { 
                Write-Host "      $_" -ForegroundColor Gray 
            }
        }
        
        # Mostrar logs de Docker si aplica
        if ($StartupMethod -eq "docker") {
            Write-Host "   Logs del contenedor:" -ForegroundColor Yellow
            docker logs $CONTAINER_NAME --tail 10
        }
        
        Complete-Cleanup
        exit 1
    }
    
    Write-Host ""
}

# PASO 8: Verificar salud completa
function Test-SpringBootHealth {
    Write-Host "8. Verificacion de salud..." -ForegroundColor Blue
    
    $checks = @(
        @{ Name = "Payments Health"; Url = "/api/v1/payments/health" },
        @{ Name = "Credit Cards Health"; Url = "/api/v1/credit-cards/health" },
        @{ Name = "Swagger UI"; Url = "/swagger-ui.html" }
    )
    
    $allOk = $true
    
    foreach ($check in $checks) {
        Write-Host "   $($check.Name)..." -NoNewline -ForegroundColor Yellow
        
        try {
            $response = Invoke-WebRequest -Uri "$API_BASE_URL$($check.Url)" -TimeoutSec 10 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-Host " OK" -ForegroundColor Green
            } else {
                Write-Host " HTTP $($response.StatusCode)" -ForegroundColor Yellow
            }
        } catch {
            Write-Host " FALLO" -ForegroundColor Red
            $allOk = $false
        }
    }
    
    if ($allOk) {
        Write-Host "   Todos los checks OK" -ForegroundColor Green
    } else {
        Write-Host "   Algunos checks fallaron" -ForegroundColor Yellow
    }
    
    Write-Host ""
    return $allOk
}

# PASO 9: Ejecutar tests E2E
function Run-E2ETests {
    Write-Host "9. Ejecutando tests E2E..." -ForegroundColor Blue
    
    $env:API_BASE_URL = $API_BASE_URL
    $env:CI = "true"
    
    Write-Host "   Comando: npx playwright test tests/api/" -ForegroundColor Cyan
    npx playwright test tests/api/ --reporter=html,line
    
    $testResult = $LASTEXITCODE
    
    if ($testResult -eq 0) {
        Write-Host "   TODOS LOS TESTS PASARON" -ForegroundColor Green
        $success = $true
    } else {
        Write-Host "   ALGUNOS TESTS FALLARON" -ForegroundColor Red
        $success = $false
    }
    
    # Mostrar reporte
    if (Test-Path "playwright-report/index.html") {
        Write-Host "   Reporte: playwright-report/index.html" -ForegroundColor Blue
        Write-Host "   Ver reporte: npx playwright show-report" -ForegroundColor Blue
    }
    
    Write-Host ""
    return $success
}

# FUNCION PRINCIPAL
try {
    $startTime = Get-Date
    
    Test-Dependencies
    Initial-Cleanup
    Start-Docker
    Build-Application
    Install-NodeDependencies
    Install-Playwright
    
    # Seleccionar metodo de inicio
    switch ($StartupMethod) {
        "maven" { Start-SpringBoot-Maven }
        "jar" { Start-SpringBoot-Jar }
        "docker" { Start-SpringBoot-Docker }
    }
    
    # Verificar salud
    $healthOk = Test-SpringBootHealth
    
    # Ejecutar tests
    $testsOk = Run-E2ETests
    
    # Resumen final
    $endTime = Get-Date
    $duration = $endTime - $startTime
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "RESUMEN FINAL" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Duracion: $($duration.ToString('mm\:ss'))" -ForegroundColor Cyan
    Write-Host "Metodo: $StartupMethod" -ForegroundColor Cyan
    Write-Host "Health: $(if ($healthOk) { 'OK' } else { 'Fallos' })" -ForegroundColor Cyan
    Write-Host "Tests: $(if ($testsOk) { 'PASARON' } else { 'FALLARON' })" -ForegroundColor Cyan
    
    if ($testsOk -and $healthOk) {
        Write-Host ""
        Write-Host "EXITO TOTAL!" -ForegroundColor Green
        $exitCode = 0
    } else {
        Write-Host ""
        Write-Host "HAY FALLOS" -ForegroundColor Red
        $exitCode = 1
    }
    
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    $exitCode = 1
} finally {
    Complete-Cleanup
}

Write-Host "========================================" -ForegroundColor Cyan
exit $exitCode 