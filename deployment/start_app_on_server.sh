#!/bin/bash

cd $(dirname $0)
docker-compose kill
docker-compose up -d