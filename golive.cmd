@echo off
setlocal
cd /d "%~dp0legacy"
echo [1/4] Freeing port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do taskkill /F /PID %%a 2^>nul
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul
echo [2/4] Compiling legacy app...
javac -nowarn QuotationApplet.java QuotationApp.java QuotationServer.java > compile.log 2>&1
type compile.log
if errorlevel 1 (
  echo COMPILE FAILED - see compile.log above.
  exit /b 1
)
echo COMPILE OK
echo [3/4] Starting server on http://localhost:8080 ...
start "QuotationServer-8080" /min cmd /c "java QuotationServer > server.log 2>&1"
echo Waiting for server to respond...
for /l %%i in (1,1,15) do (
  timeout /t 1 /nobreak >nul
  curl -s -o nul -m 2 http://localhost:8080/api/quotes
  if not errorlevel 1 goto :serverup
)
echo SERVER DID NOT START. server.log:
type server.log
exit /b 1
:serverup
echo SERVER UP at http://localhost:8080
echo [4/4] Posting test quote...
curl -s -m 5 -X POST http://localhost:8080/api/quote -H "Content-Type: text/plain" --data-binary @testbody.txt > testquote.log 2>&1
type testquote.log
echo.
echo --- HISTORY ---
curl -s -m 5 http://localhost:8080/api/quotes
echo.
echo DONE. Server keeps running in QuotationServer-8080 window.
echo Launch GUI yourself with: java QuotationApp

