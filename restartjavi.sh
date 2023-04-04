#!/bin/bash

docker-compose -f docker-compose-devel-mongo.yml down
make 
docker-compose -f docker-compose-devel-mongo.yml build
docker-compose -f docker-compose-devel-mongo.yml up

