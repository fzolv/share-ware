#!/bin/bash

# Launch all services in the background
pid=$(lsof -ti tcp:8080)
if [ -n "$pid" ]; then
  kill $pid
  echo "Killed process on port $port (PID: $pid)"
fi

echo "All services stopped."