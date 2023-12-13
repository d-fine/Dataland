import pika
import os

rabbit_mq_connection_parameters = pika.ConnectionParameters(
    host="rabbitmq",
    port=5672,
    virtual_host="vhost",
    credentials=pika.credentials.PlainCredentials(os.environ["RABBITMQ_USER"], os.environ["RABBITMQ_PASS"]),
    connection_attempts=100000,
    retry_delay=5,
)
