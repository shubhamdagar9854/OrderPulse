#!/bin/bash
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOGS_DIR="$ROOT_DIR/logs"
mkdir -p "$LOGS_DIR"

echo ""
echo " ================================================="
echo "  Starting OrderPulse - All Services"
echo " ================================================="
echo ""


# Step 1: Start all microservices in background
echo ""
echo "  [2/3] Starting Microservices..."
SERVICES=("api-gateway" "user-service" "product-service" "order-service" "payment-service" "notification-service")
for name in "${SERVICES[@]}"; do
    cd "$ROOT_DIR/$name"
    nohup mvn spring-boot:run > "$LOGS_DIR/$name.log" 2>&1 &
    echo "        $name started (PID $!)"
    sleep 3
done

# Step 2: Start Frontend
echo ""
echo "  [3/3] Starting Frontend..."
cd "$ROOT_DIR/frontend"
nohup python3 -m http.server 5500 > "$LOGS_DIR/frontend.log" 2>&1 &
echo "        Frontend started (PID $!, http://localhost:5500)"

echo ""
echo " ================================================="
echo "  ALL SERVICES ARE STARTING"
echo " ================================================="
echo "  Then open http://localhost:5500 in your browser"
echo ""
echo "  Check logs:   tail -f $LOGS_DIR/<service-name>.log"
echo "  Stop all:     ./stop-all.sh"
echo " ================================================="
