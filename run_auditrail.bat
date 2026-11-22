@echo off
title AuditRail - Enterprise One-Click Orchestrator
color 0b

echo ===============================================================================
echo            AuditRail Enterprise Tamper-Evident Platform Launcher
echo ===============================================================================
echo.
echo  Checking Docker environment...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo  [ERROR] Docker is not detected or Docker Desktop is not running.
    echo  Please start Docker Desktop and run this script again.
    echo.
    pause
    exit /b 1
)

echo  [1/3] Spinning up containerized ecosystem via Docker Compose...
echo        - MySQL 8.0 (InnoDB Append-Only Engine)
echo        - MinIO WORM S3 Object Lock Emulator (port 9000/9001)
echo        - AuditRail Core Backend (Spring Boot 3.3.5 / port 8080)
echo        - AuditRail Editorial UI Console (Nginx Alpine / port 80 and 5173)
echo.
docker compose up -d --build

if %errorlevel% neq 0 (
    echo.
    echo  [ERROR] Failed to start Docker Compose containers.
    pause
    exit /b 1
)

echo.
echo  [2/3] Waiting for backend services to become healthy...
timeout /t 10 /nobreak >nul

echo.
echo  [3/3] Opening AuditRail in your default browser...
start http://localhost:5173
start http://localhost:8080/api/v1/policy/dpdp

echo.
echo ===============================================================================
echo  SUCCESS! AuditRail is running:
echo    - Editorial UI Console:     http://localhost:5173 (or http://localhost)
echo    - Backend API & Actuator:   http://localhost:8080
echo    - MinIO S3 Object Lock UI:  http://localhost:9001 (minioadmin / minioadminpassword2026!)
echo    - Simulated Client App:     Double-click 'launch_simulated_client_app.bat'
echo ===============================================================================
echo.
echo  To shut down the entire stack later, simply run:
echo    docker compose down
echo.
pause
