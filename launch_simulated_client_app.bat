@echo off
title AuditRail - Simulated Client Banking App & Isolation Showcase
color 0b

echo ===============================================================================
echo                AuditRail Simulated Client Banking Showcase
echo                      Enterprise Multi-Tenant Isolation
echo ===============================================================================
echo.
echo  [1/2] Checking Python environment...
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo  [!] Python 3 is required to launch the simulated client app.
    pause
    exit /b 1
)

echo  [2/2] Launching Simulated Client Banking Core on http://localhost:5050...
echo.
echo  -----------------------------------------------------------------------------
echo  * Demonstrates how client databases automatically push audit events to AuditRail
echo  * Includes Tenant Isolation toggle: Apex Bank (FinTech) vs Horizon (Health)
echo  * Includes DBA SQL Tamper & Row Deletion attack simulation buttons
echo  -----------------------------------------------------------------------------
echo.

cd /d "%~dp0demo-client-app\frontend"
start http://localhost:5050/index.html
python server.py

pause
