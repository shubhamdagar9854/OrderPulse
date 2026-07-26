#!/bin/bash
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOGS_DIR="$ROOT_DIR/logs"
mkdir -p "$LOGS_DIR"

echo ""
echo " ================================================="
echo "  Starting OrderPulse - All Services"
echo " ================================================="
echo ""

# Step 1: Start Eureka Server first
echo "  [1/3] Starting Eureka Server..."
cd "$ROOT_DIR/eureka-server"
nohup mvn spring-boot:run > "$LOGS_DIR/eureka-server.log" 2>&1 &
echo "        Eureka Server started (PID $!)"

echo "        Waiting for Eureka to be ready (this can take 30-40 sec)..."
for i in $(seq 1 30); do
    if curl -s http://localhost:8761/actuator/health > /dev/null 2>&1; then
        echo "        Eureka is ready!"
        break
    fi
    sleep 2
done

# Step 2: Start all microservices in background
echo ""
echo "  [2/3] Starting Microservices..."
SERVICES=("api-gateway" "user-service" "product-service" "order-service" "payment-service" "notification-service")
for name in "${SERVICES[@]}"; do
    cd "$ROOT_DIR/$name"
    nohup mvn spring-boot:run > "$LOGS_DIR/$name.log" 2>&1 &
    echo "        $name started (PID $!)"
    sleep 3
done

# Step 3: Start Frontend
echo ""
echo "  [3/3] Starting Frontend..."
cd "$ROOT_DIR/frontend"
nohup python3 -m http.server 5500 > "$LOGS_DIR/frontend.log" 2>&1 &
echo "        Frontend started (PID $!, http://localhost:5500)"

echo ""
echo " ================================================="
echo "  ALL SERVICES ARE STARTING"
echo " ================================================="
echo "  Wait 1-2 min for all services to register in Eureka"
echo "  Then open http://localhost:5500 in your browser"
echo ""
echo "  Check logs:   tail -f $LOGS_DIR/<service-name>.log"
echo "  Stop all:     ./stop-all.sh"
echo " ================================================="
