#!/bin/sh

echo "Shutting down API..."
play stop
test -f server.pid && kill -15 $(cat "server.pid") 
rm -rf server.pid

if [ "$1" == "destroy" ] 
then
echo "Destroying Docker containers..."
cd docker && docker compose down -v --remove-orphans 
rm -rf redis
else
echo "Bring down Docker containers..."
cd docker && docker compose stop
fi

cd ..
echo "Shutdown complete..."
