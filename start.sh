#!/bin/bash
RED='\033[1;31m'
GREEN='\033[1;32m'
YELLOW='\033[1;33m'
CYAN='\033[1;94m'
RESET='\033[0m'

INFO="[${CYAN}INFO${RESET}]"
WARNING="[${YELLOW}WARNING${RESET}]"
ERROR="[${RED}ERROR${RESET}]"
SUCCESS="[${GREEN}SUCCESS${RESET}]"

# Check if running from cmd on windows
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
	echo -e "${ERROR} This script must be run in WSL or Git Bash."
	exit 1
fi

# Get absolute path of the script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR || exit

SERVICE_PIDS=()
cleanup() {
	echo -e "${WARNING} Caught exit signal. Stopping services..."

	cd ${SCRIPT_DIR}
	docker compose -f database/docker-compose.yml down
	for pid in "${SERVICE_PIDS[@]}"; do
		echo -e "${INFO} Stopping service with PID $pid..."
		kill "$pid" 2>/dev/null
	done

	echo -e "${SUCCESS} All services stopped."
	exit 0
}

trap cleanup SIGINT SIGTERM

echo -e "${INFO} Exporting environmental variables..."
set -a
source database/.env
source recipe-service/.env
source user-service/.env
set +a

echo -e "${INFO} Starting MySQL container..."
cd database

echo -e "${INFO} Creating MySQL configuration file..."
envsubst <./init.template.sql >./init.sql

echo -e "${INFO} Building Mysql Image..."
docker build -t my-mysql:v1 .

echo -e "${INFO} Starting MySQL container..."
docker compose -f docker-compose.yml up -d --build

# Check if the container started successfully
if [ $? -eq 0 ]; then
	echo -e "${SUCCESS} MySQL container started successfully."
else
	echo -e "${ERROR} Failed to start MySQL container."
	exit 1
fi

# Wait for the MySQL container to be ready
echo -e "${INFO} Waiting for MySQL container to be ready..."
until docker exec mysql mysqladmin ping --silent; do
	sleep 1
done

echo -e "${INFO} Cleaning up loose files..."
rm -f ./init.sql

cd ..

echo -e "${INFO} Compiling source code..."
mvn compile

wait_for_port() {
	local PORT=$1
	local NAME=$2
	local COUNTER=0
	local MAX_WAIT=30

	until nc -z localhost "$PORT" >/dev/null 2>&1; do
		echo -e "${INFO} [${COUNTER}/${MAX_WAIT}] Waiting for $NAME to be ready..."
		sleep 1
		COUNTER=$((COUNTER + 1))
		if [ $COUNTER -ge $MAX_WAIT ]; then
			echo -e "${ERROR} $NAME failed to start within ${MAX_WAIT} seconds."
			exit 1
		fi
	done
	echo -e "${SUCCESS} $NAME is ready."
}

echo -e "${INFO} Starting recipe-service"
cd recipe-service
mvn spring-boot:run >/dev/null 2>&1 &
SERVICE_PIDS+=($!)
wait_for_port 8082 "recipe-service"
cd ..

echo -e "${INFO} Starting user-service..."
cd user-service
mvn spring-boot:run >/dev/null 2>&1 &
SERVICE_PIDS+=($!)
wait_for_port 8081 "user-service"
cd ..

echo -e "${INFO} Starting api-gateway..."

cd api-gateway
mvn spring-boot:run >/dev/null 2>&1 &
SERVICE_PIDS+=($!)
wait_for_port 8080 "api-gateway"
cd ..

echo -e "${INFO} All services are up and running."

wait
