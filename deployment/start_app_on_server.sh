#!/bin/bash

docker kill $(docker ps -q)
docker rm $(docker ps -a -q)
docker -d --restart always --name backend -v /home/ubuntu/jar:/jar -p80:8080 eclipse-temurin:17.0.2_8-jre-alpine java -jar /jar/dala-backend.jar
