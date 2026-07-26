#!/bin/bash
echo ""
echo " Stopping all OrderPulse services..."
echo ""

PORTS=(8761 8080 8081 8082 8083 8084 8085)
for port in "${PORTS[@]}"; do
    pid=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$pid" ]; then
        kill -9 $pid 2>/dev/null
        echo "  Port $port - Stopped (PID $pid)"
    else
        echo "  Port $port - Not running"
    fi
done

echo ""
echo " All services stopped"
echo ""
