#!/bin/bash
# One-click runner (macOS): kill stale server, compile, start server, open app.
# First time on Mac: chmod +x golive.command
cd "$(dirname "$0")/legacy" || { echo "legacy/ folder not found"; exit 1; }

echo "[1/4] Freeing port 8080..."
lsof -ti:8080 | xargs kill -9 2>/dev/null
sleep 1

echo "[2/4] Compiling legacy app..."
javac -nowarn QuotationApplet.java QuotationApp.java QuotationServer.java || { echo "COMPILE FAILED"; exit 1; }
echo "COMPILE OK"

echo "[3/4] Starting server on http://localhost:8080 ..."
java QuotationServer > server.log 2>&1 &
SERVER_PID=$!
OK=0
for i in $(seq 1 15); do
  sleep 1
  if curl -s -m 2 http://localhost:8080/api/quotes >/dev/null 2>&1; then OK=1; break; fi
done
if [ "$OK" -ne 1 ]; then
  echo "SERVER DID NOT START. server.log:"
  cat server.log
  kill $SERVER_PID 2>/dev/null
  exit 1
fi
echo "SERVER UP at http://localhost:8080 (pid $SERVER_PID)"
echo "[4/4] Posting test quote..."
curl -s -m 5 -X POST http://localhost:8080/api/quote -H "Content-Type: text/plain" --data-binary @testbody.txt | tee testquote.log
echo ""
echo "--- HISTORY ---"
curl -s -m 5 http://localhost:8080/api/quotes
echo ""
echo "DONE. Server keeps running (pid $SERVER_PID). Stop with: kill $SERVER_PID"
echo "Launch GUI yourself with: java QuotationApp"
