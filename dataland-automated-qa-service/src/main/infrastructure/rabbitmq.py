import logging
from pika.connection import Parameters
from pika.adapters.blocking_connection import BlockingChannel, BlockingConnection
from pika.spec import Basic, BasicProperties
from typing import Callable


class RabbitMq:
    """
    A wrapper for message queue interactions
    """

    def __init__(self, connection_parameters: Parameters) -> None:
        """
        Initializes the instance
        :param connection_parameters: The parameters that will be used to establish a connection to the message queue
        """
        self._connection_parameters = connection_parameters
        self._connection: BlockingConnection | None = None
        self._channel: BlockingChannel | None = None

    def connect(self) -> None:
        """
        Establishes a connection to the message queue using the specified connection_parameters
        """
        logging.info("Connecting to RabbitMQ...")
        self._connection = BlockingConnection(self._connection_parameters)
        self._channel = self._connection.channel()
        logging.info("Connection established")

    def register_receiver(
        self,
        exchange: str,
        routing_key: str,
        queue_name_prefix: str,
        callback: Callable[[BlockingChannel, Basic.Deliver, BasicProperties, bytes], None],
    ) -> None:
        """
        Registers a receiver callback on a specific queue

        :param exchange: the exchange sending messages to this queue
        :param routing_key: the routing key
        :param queue_name_prefix: the purpose specific prefix of the queue name
        :param callback: the function to call when a message is received on the queue
        """
        queue = f"{queue_name_prefix}AutomatedQaService"
        self._channel.queue_declare(queue=queue, durable=True)
        self._channel.queue_bind(queue=queue, exchange=exchange, routing_key=routing_key)
        self._channel.basic_consume(queue=queue, on_message_callback=callback)

    def consume_loop(self) -> None:
        """
        Starts the loop for consuming messages.
        """
        self._channel.start_consuming()
