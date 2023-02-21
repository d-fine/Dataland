#!/bin/bash
sed -i 's/RABBITMQ_USER_PLACEHOLDER/'"$RABBITMQ_USER"'/g' /etc/rabbitmq/definitions.json
sed -i 's/RABBITMQ_PASS_HASH_PLACEHOLDER/'"$RABBITMQ_PASS_HASH"'/g' /etc/rabbitmq/definitions.json
echo Definitions file used for initiation of RabbitMQ:
cat /etc/rabbitmq/definitions.json
docker-entrypoint.sh $1
