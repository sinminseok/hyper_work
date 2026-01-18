#!/bin/bash
set -e

echo "========== AfterInstall: Setting up deployment =========="

cd /home/ubuntu/funy-run

# Ensure correct permissions
chown -R ubuntu:ubuntu /home/ubuntu/funy-run

# Make scripts executable
chmod +x /home/ubuntu/funy-run/scripts/*.sh 2>/dev/null || true

echo "========== AfterInstall: Completed =========="
