#!/bin/bash
set -e

echo "========== ApplicationStart: Starting containers =========="

cd /home/ubuntu/funy-run

# Pull latest images
docker compose pull

# Start containers
docker compose up -d

# Cleanup unused images
docker image prune -f

echo "========== ApplicationStart: Completed =========="
