#!/bin/bash
set -e

echo "========== BeforeInstall: Stopping existing containers =========="

cd /home/ubuntu/funy-run || exit 0

# Stop and remove existing containers
if [ -f docker-compose.yml ]; then
    docker-compose down --remove-orphans || true
fi

# Force remove containers if they still exist
docker rm -f funy-run-api funy-run-admin redis 2>/dev/null || true

# Kill processes on ports 8080, 8081
fuser -k 8080/tcp 2>/dev/null || true
fuser -k 8081/tcp 2>/dev/null || true

# Wait for ports to be released
sleep 3

echo "========== BeforeInstall: Completed =========="
