#!/bin/bash

# Launch all services in the background
echo "Starting iam-service..."
mvn -pl iam-service spring-boot:run > iam-service.log 2>&1 &
sleep 5

echo "Starting user-service..."
mvn -pl user-service spring-boot:run > user-service.log 2>&1 &
sleep 5

echo "Starting group-service..."
mvn -pl group-service spring-boot:run > group-service.log 2>&1 &
sleep 5

echo "Starting expense-service..."
mvn -pl expense-service spring-boot:run > expense-service.log 2>&1 &
sleep 5

echo "Starting balance-service..."
mvn -pl balance-service spring-boot:run > balance-service.log 2>&1 &
sleep 5

echo "All services started. Tailing logs..."
tail -f user-service.log group-service.log expense-service.log balance-service.log iam-service.log