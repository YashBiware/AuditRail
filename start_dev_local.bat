@echo off
title AuditRail - Fast Local Dev Launcher (No Docker Required)
color 0b

echo ===============================================================================
echo                 AuditRail Fast Local Development Launcher
echo       (Zero Docker Required - Uses Built-In H2 Engine ^& AWS Emulator)
echo ===============================================================================
echo.

echo  [1/2] Starting Spring Boot Backend on http://localhost:8080 (Profile: dev)...
start "AuditRail-Backend" cmd /k "cd /d %~dp0backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

echo  Waiting 10 seconds for backend to initialize database...
timeout /t 10 /nobreak >nul

echo.
echo  [2/2] Starting Frontend Console on http://localhost:5173...
start "AuditRail-Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"

echo.
echo ===============================================================================
echo   Pre-configured Default Accounts Available:
echo     1) Acme Global Logistics
echo        Email:    admin@acme-logistics.com
echo        Password: Admin@AuditRail2026!
echo.
echo     2) Apex National Bank
echo        Email:    admin@apexbank.com
echo        Password: Admin@AuditRail2026!
echo.
echo     Or register ANY new organization directly from the UI!
echo ===============================================================================
echo.
echo Opening browser...
timeout /t 3 /nobreak >nul
start http://localhost:5173
pause
