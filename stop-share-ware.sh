#!/bin/bash

echo "Stopping all share-ware services..."


# Kill processes running on ports 8080-8084
for port in 8080 8081 8082 8083 8084; do
	pid=$(lsof -ti tcp:$port)
	if [ -n "$pid" ]; then
		kill $pid
		echo "Killed process on port $port (PID: $pid)"
	fi
done

# Also kill all Java processes started by Maven spring-boot:run
ps aux | grep 'mvn.*spring-boot:run' | grep -v grep | awk '{print $2}' | xargs -r kill

echo "All services stopped."
