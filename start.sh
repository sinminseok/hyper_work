#!/bin/bash

API_MODULE_DIR="./run_api"
ADMIN_API_MODULE_DIR="./run_admin"

echo "Cleaning and building the jar with Gradle..."
./gradlew clean bootJar -p "$API_MODULE_DIR" || {
  echo "Gradle bootJar build failed!"
  exit 1
}

echo "Building the jar (excluding tests)..."
./gradlew -p "$API_MODULE_DIR" -x test || {
  echo "Gradle build failed!"
  exit 1
}

echo "Cleaning and building the jar with Gradle..."
./gradlew clean bootJar -p "$ADMIN_API_MODULE_DIR" || {
  echo "Gradle bootJar build failed!"
  exit 1
}

echo "Building the jar (excluding tests)..."
./gradlew -p "$ADMIN_API_MODULE_DIR" -x test || {
  echo "Gradle build failed!"
  exit 1
}

echo "Building Docker images..."
docker compose -f docker-compose-local.yml build --no-cache || {
  echo "Docker build failed!"
  exit 1
}

echo "Starting containers..."
docker compose -f docker-compose-local.yml up -d || {
  echo "Docker compose up failed!"
  exit 1
}

echo "All done! Containers are up and running."
