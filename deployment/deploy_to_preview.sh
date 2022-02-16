#!/bin/bash
mkdir -p ~/.ssh/
echo "3.71.162.94 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBNGocXXehCSfKoYwGdaYUpjvNm7gZE2LS7Nl/gGGXSxqwbGT+X6b+q7AGwhwZpFY9u17wv4NY3EOCK1cGaeot4k=" >  ~/.ssh/known_hosts
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa

ssh ubuntu@3.71.162.94 ls -lisa
scp "$1" ubuntu@3.71.162.94:/home/ubuntu/jar/dala-backend.jar
ssh ubuntu@3.71.162.94 docker kill $(docker ps -q) || docker rm $(docker ps -a -q)
ssh ubuntu@3.71.162.94 docker -d --restart always --name backend -v /home/ubuntu/jar:/jar -p80:8080 eclipse-temurin:17.0.2_8-jre-alpine java -jar /jar/dala-backend.jar
