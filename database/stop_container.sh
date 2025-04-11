#!/bin/bash
RED='\033[31m'
GREEN='\033[32m'
YELLOW='\033[33m'
CYAN='\033[36m'
RESET='\033[0m'

# Check if running from cmd on windows
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
  echo -e "${RED}[ERROR]${RESET} This script must be run in WSL or Git Bash."
  exit 1
fi

# Check if container is running
if [ "$(docker ps -q -f name=mysql)" ]; then
  echo -e "${CYAN}[INFO]${RESET} MySQL container is found, Stopping it..."
  if [ "$1" == "-v" ]; then
    echo -e "${CYAN}[INFO]${RESET} Removing container and volumes..."
    docker compose -f docker-compose.yml --env-file ../.env down -v
  else
    echo -e "${CYAN}[INFO]${RESET} Removing container..."
    docker compose -f docker-compose.yml --env-file ../.env down
  fi
  if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR]${RESET} Failed to stop MySQL container."
    exit 1
  fi
  echo -e "${GREEN}[SUCCESS]${RESET} MySQL container stopped successfully."
else
  echo -e "${CYAN}[INFO]${RESET} MySQL container is not running."
fi