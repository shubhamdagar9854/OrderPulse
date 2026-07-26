@echo off
title OrderPulse
cd /d "%~dp0"
if not exist "logs" mkdir logs
setlocal enabledelayedexpansion

echo ================================================
echo   ORDERPULSE - Starting All Services
echo ================================================
echo.

where java >nul 2>&1
if errorlevel 1 (echo [ERROR] Java not found & pause & exit /b 1)

echo [START] Starting services...
echo.

set SERVICES=user-service:8081 product-service:8082 order-service:8083 payment-service:8084 notification-service:8085 api-gateway:8080

for %%a in (%SERVICES%) do (
    for /f "tokens=1,2 delims=:" %%s in ("%%a") do (
        set "NAME=%%s"
        set "PORT=%%t"
        
        if exist "%%s\mvnw.cmd" (
            set "MVN=%%s\mvnw.cmd"
        ) else (
            set "MVN=mvn"
        )

        echo    Starting %%s on port %%t...
        start "" /b cmd /c "cd /d "%%s" && !MVN! spring-boot:run > ..\logs\%%s.log 2>&1"
        timeout /t 2 >nul
    )
)

REM Frontend
echo.
echo [DONE] Starting Frontend on port 5500...

set PYTHON_CMD=python
where python3 >nul 2>&1
if not errorlevel 1 set PYTHON_CMD=python3

start "" /b cmd /c "cd /d "%~dp0frontend" && %PYTHON_CMD% -m http.server 5500 > ..\logs\frontend.log 2>&1"

cls
echo ================================================
echo   ALL SERVICES ARE STARTING
echo ================================================
echo.
echo   Open:   http://localhost:5500
echo   Login:  admin@orderpulse.com / admin123
echo.
echo   Wait 1-2 min for services to fully start
echo.
echo   Stop:   close this window or run stop-all.bat
echo ================================================
endlocal
