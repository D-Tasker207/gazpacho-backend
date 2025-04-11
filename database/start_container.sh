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

# Check if container is already running
if [ "$(docker ps -q -f name=mysql-container)" ]; then
  echo -e "${YELLOW}[WARNING]${RESET} MySQL container is already running. Stopping it..."
  docker compose -f docker-compose.yml --env-file ../.env down
  if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR]${RESET} Failed to stop MySQL container."
    exit 1
  fi
  echo -e "${GREEN}[SUCCESS]${RESET} MySQL container stopped successfully."
fi

echo -e "${CYAN}[INFO]${RESET} Starting MySQL container..."

echo -e "${CYAN}[INFO]${RESET} Preparing Initialization SQL script..."

# Check if the required environment variables are set
if [ -z "$MYSQL_ROOT_PASSWORD" ]; then
  echo -e "${YELLOW}[WARNING]${RESET} MYSQL_ROOT_PASSWORD is not set."
fi
# Otherwise try to load the env file at path ../.env
if [ -f ../.env ]; then
  echo -e "${CYAN}[INFO]${RESET} Attempting environment variables from ../.env"
  export $(grep -v '^#' ../.env | xargs)
# Otherwise return with an error
else
  echo -e "${RED}[ERROR]${RESET} No environment file found. Please set MYSQL_ROOT_PASSWORD or provide a .env file at ../.env"
  exit 1
fi

# Check if init.sql is a directory for some reason (this keeps on happening for some reason)
if [ -d "./init.sql" ]; then
  echo -e "${RED}[ERROR]${RESET} 'init.sql' is a directory. Removing it so we can write the file."
  rm -r ./init.sql
fi
# Check if init.sql already exists
if [ -f "./init.sql" ]; then
  echo -e "${YELLOW}[WARNING]${RESET} 'init.sql' already exists. Removing it so we can write the file."
  rm -f ./init.sql
fi
echo -e "${CYAN}[INFO]${RESET} Creating MySQL configuration file..."
envsubst < ./init.template.sql > ./init.sql

echo -e "${CYAN}[INFO]${RESET} Starting MySQL container..."
docker compose -f docker-compose.yml --env-file ../.env up -d --build

# Check if the container started successfully
if [ $? -eq 0 ]; then
  echo -e "${GREEN}[SUCCESS]${RESET} MySQL container started successfully."
else
  echo -e "${RED}[ERROR]${RESET} Failed to start MySQL container."
  exit 1
fi

# Wait for the MySQL container to be ready
echo -e "${CYAN}[INFO]${RESET} Waiting for MySQL container to be ready..."
until docker exec mysql mysqladmin ping --silent; do
  sleep 1
done

# Clean up the initialization SQL script
echo -e "${CYAN}[INFO]${RESET} Cleaning up initialization SQL script..."
rm -f ./init.sql
if [ $? -ne 0 ]; then
  echo -e "${RED}[ERROR]${RESET} Failed to remove initialization SQL script."
  exit 1
fi

echo -e "${GREEN}[SUCCESS]${RESET} MySQL container is ready."
#Print these if script was not called by another script
if [ "$0" = "$BASH_SOURCE" ]; then
  echo -e "${CYAN}[INFO]${RESET} To connect to the MySQL container, use the following command:"
  echo -e "${CYAN}[INFO]${RESET} docker exec -it mysql-container mysql -u root -p"
  echo -e "${CYAN}[INFO]${RESET} To stop the container, run:"
  echo -e "${CYAN}[INFO]${RESET} docker compose down"
  echo -e "${CYAN}[INFO]${RESET} To remove the container and its volumes, run:"
  echo -e "${CYAN}[INFO]${RESET} docker compose down -v"
fi