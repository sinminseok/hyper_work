#!/bin/bash
set -e

echo "========== ValidateService: Checking service health =========="

# Wait for services to start
sleep 10

# Check if containers are running
if ! docker ps | grep -q funy-run-api; then
    echo "ERROR: funy-run-api container is not running"
    exit 1
fi

if ! docker ps | grep -q funy-run-admin; then
    echo "ERROR: funy-run-admin container is not running"
    exit 1
fi

# Health check for API server
for i in {1..5}; do
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "API server is healthy"
        break
    fi
    if [ $i -eq 5 ]; then
        echo "WARNING: API health check failed after 5 attempts"
    fi
    sleep 5
done

# Health check for Admin server
for i in {1..5}; do
    if curl -sf http://localhost:8081/actuator/health > /dev/null 2>&1; then
        echo "Admin server is healthy"
        break
    fi
    if [ $i -eq 5 ]; then
        echo "WARNING: Admin health check failed after 5 attempts"
    fi
    sleep 5
done

echo "========== ValidateService: Completed =========="
