#!/bin/bash
make
docker-compose -f docker-compose-devel-mongo.yml build
docker-compose -f docker-compose-devel-mongo.yml up
