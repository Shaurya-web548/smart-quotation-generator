@echo off
cd /d "%~dp0"

echo Compiling...
javac *.java
if errorlevel 1 (
    echo Compile failed. Check the errors above.
    pause
    exit /b 1
)

echo Starting server...
start "Quotation Server" java QuotationServer
timeout /t 3 /nobreak >nul

echo Launching app...
appletviewer http://localhost:8080/index.html
