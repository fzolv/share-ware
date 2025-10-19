#!/bin/bash

# Launch all services in the background
echo "Starting Hull..."
mvn -pl hull spring-boot:run > share-ware.log 2>&1 &
echo "All services started. Tailing logs..."
