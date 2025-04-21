#!/bin/bash
RED='\033[31m'
GREEN='\033[32m'
YELLOW='\033[33m'
CYAN='\033[36m'
RESET='\033[0m'

INFO="${CYAN}[INFO]${RESET}"
WARNING="${YELLOW}[WARNING]${RESET}"
ERROR="${RED}[ERROR]${RESET}"
SUCCESS="${GREEN}[SUCCESS]${RESET}"

# Check if running from cmd on windows
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
	echo -e "${ERROR} This script must be run in WSL or Git Bash."
	exit 1
fi

# Get absolute path of the script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit

# Check if container is already running
if [ "$(docker ps -q -f name=mysql-container)" ]; then
	echo -e "${WARNING} MySQL container is already running. Stopping it..."
	docker compose -f docker-compose.yml down
	if [ $? -ne 0 ]; then
		echo -e "${ERROR} Failed to stop MySQL container."
		exit 1
	fi
	echo -e "${GREEN}[SUCCESS]${RESET} MySQL container stopped successfully."
fi

echo -e "${INFO} Starting MySQL container..."

echo -e "${INFO} Preparing Initialization SQL script..."

export_env_vars(){
  local file="$1"
  local prefix="$2"
  while IFS= read -r line; do
    if [[ "$line"=="$prefix"* ]]; then
      export "$line"
    fi
  done < "$file"
}

# Check if the required environment variables are set
if [ -z "$MYSQL_ROOT_PASSWORD" ]; then
	echo -e "${WARNING} MYSQL_ROOT_PASSWORD is not set."
fi
# Otherwise try to load the env file at path ../.env
if [ -f .env ]; then
	echo -e "${INFO} Attempting environment variables from .env"
  export_env_vars .env "MYSQL_"
# Otherwise return with an error
else
	echo -e "${ERROR} No environment file found. Please set MYSQL_ROOT_PASSWORD or provide a .env file in the current directory"
	exit 1
fi

#Check if userdb_username and recipedb_username are set
if [ -z "$USERDB_USERNAME" ]; then
	echo -e "${INFO} UserDB Credentials not found, attempting to read from user-service/.env"
  
  if [ -f ../user-service/.env ]; then
    export_env_vars ../user-service/.env "USERDB_"
  else
    echo -e "${ERROR} No environment file found in ../user-service/.env"
    exit 1
  fi
fi
if [ -z "$RECIPEDB_USERNAME" ]; then
  echo -e "${INFO} RecipeDB Credentials not found, attempting to read from recipe-service/.env"

  if [ -f ../recipe-service/.env ]; then
    export_env_vars ../recipe-service/.env "RECIPEDB_"
  else
    echo -e "${ERROR} No environment file found in ../user-service/.env"
    exit 1
  fi
fi

# Check if init.sql is a directory for some reason (this keeps on happening for some reason)
if [ -d "./init.sql" ]; then
	echo -e "${ERROR} 'init.sql' is a directory. Removing it so we can write the file."
	rm -r ./init.sql
fi
# Check if init.sql already exists
if [ -f "./init.sql" ]; then
	echo -e "${WARNING} 'init.sql' already exists. Removing it so we can write the file."
	rm -f ./init.sql
fi
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

# Clean up the initialization SQL script
echo -e "${INFO} Cleaning up initialization SQL script..."
rm -f ./init.sql
if [ $? -ne 0 ]; then
	echo -e "${ERROR} Failed to remove initialization SQL script."
	exit 1
fi

echo -e "${SUCCESS} MySQL container is ready."
#Print these if script was not called by another script
if [ "$0" = "$BASH_SOURCE" ]; then
	echo -e "${INFO} To connect to the MySQL container, use the following command:"
	echo -e "${INFO} mysql -h 127.0.0.1 -P 3306 -u root -p"
	echo -e "${INFO} To stop the container, run:"
	echo -e "${INFO} stop_container.sh"
	echo -e "${INFO} To remove the container and its volumes, run:"
	echo -e "${INFO} stop_container.sh -v"
fi
