@echo off
rem Usage: run.bat server   -> compile and start the backend
rem        run.bat app      -> compile and open the Applet in a window
cd /d "%~dp0"

if "%1"=="server" (
  javac QuotationServer.java
  java QuotationServer
  goto :eof
)
if "%1"=="app" (
  javac -nowarn QuotationApplet.java QuotationApp.java
  java QuotationApp
  goto :eof
)
echo Usage: run.bat server^|app
