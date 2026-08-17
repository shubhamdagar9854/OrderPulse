@echo off
title OrderPulse - Stopping Services
echo ================================================
echo   Stopping OrderPulse services...
echo ================================================
echo.

for %%p in (8080 8081 8082 8083 8084 8085 5500) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p "') do (
        taskkill /f /pid %%a >nul 2>&1
    )
)

echo   All services stopped.
echo ================================================
pause
