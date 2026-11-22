@echo off
title Aarohi Global Bank - Client Banking Application
color 0b

echo ===============================================================================
echo                Aarohi Global Bank - Client Banking Core
echo              Connected to AuditRail Evidentiary Ledger
echo ===============================================================================
echo.
echo  [1/2] Checking Python environment...
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo  [!] Python 3 is required to launch the simulated client app.
    pause
    exit /b 1
)

echo  [2/2] Launching Aarohi Global Bank Application on http://localhost:5050...
echo.
echo  -----------------------------------------------------------------------------
echo  * Bank: Aarohi Global Bank
echo  * Pre-configured API Key: ar_live_admin-__IWk...
echo  * Role-based access: Manager, Cashier, Employee
echo  -----------------------------------------------------------------------------
echo.

cd /d "%~dp0aarohi-bank-client\frontend"
start http://localhost:5050/index.html
python server.py

pause
