#!/bin/bash

# Launch all services in the background
echo "Starting Hull..."
mvn -pl core clean install
mvn -pl data clean install
mvn -pl bus clean install
mvn -pl hull clean install
mvn -pl hull spring-boot:run > share-ware.log 2>&1 &
echo "All services started. Tailing logs..."

tail -f share-ware.log
