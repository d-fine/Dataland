import logging
import pika.exceptions

from .properties import *


class RabbitMq:
    def __init__(self, connection_parameters: pika.connection.Parameters):
        self._connection_parameters = connection_parameters
        self._connection: pika.adapters.blocking_connection.BlockingConnection | None = None
        self._channel: pika.adapters.blocking_connection.BlockingChannel | None = None

    def connect(self):
        logging.info("Connecting to RabbitMQ...")
        self._connection = pika.BlockingConnection(self._connection_parameters)
        self._channel = self._connection.channel()
        logging.info("Connection established")

    def disconnect(self):
        try:
            self._channel.close()
            self._connection.close()
        except pika.exceptions.ChannelWrongStateError:
            logging.error("Could not close channel.")
        except pika.exceptions.ConnectionWrongStateError:
            logging.error("Could not close connection.")

    def register_receiver(self, exchange: str, routing_key: str, queue_name_prefix: str, callback):
        queue = f"{queue_name_prefix}AutomatedQaService"
        self._channel.queue_declare(queue=queue, durable=True)
        self._channel.queue_bind(queue=queue, exchange=exchange, routing_key=routing_key)
        self._channel.basic_consume(
            queue=queue,
            on_message_callback=callback
        )

    def consume_loop(self):
        self._channel.start_consuming()
