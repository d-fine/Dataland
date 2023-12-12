import time

import pika
import pika.exceptions
import properties as p

data_key = "data"
document_key = "document"
receiving_exchange = "itemStored"
manual_qa_requested_exchange = "manualQaRequested"
quality_assured_exchange = "dataQualityAssured"


def listen_to_message_queue():
    mq = RabbitMq(p.rabbit_mq_connection_parameters)
    mq.connect()
    # mq.register_receiver(receiving_exchange, data_key, qa_data)
    # mq.register_receiver(receiving_exchange, document_key, qa_document)
    mq.register_receiver(receiving_exchange, data_key, tutorial_callback)
    mq._channel.start_consuming()  # TODO handle disconnects
    mq.disconnect()


class RabbitMq:
    def __init__(self, connection_parameters: pika.connection.Parameters):
        self._connection_parameters = connection_parameters
        self._connection: pika.adapters.blocking_connection.BlockingConnection | None = None
        self._channel: pika.adapters.blocking_connection.BlockingChannel | None = None

    def connect(self):
        attempt = 1
        while True:
            try:
                print(f"Connection attempt {attempt}")
                self._connection = pika.BlockingConnection(self._connection_parameters)
                self._channel = self._connection.channel()
                break
            except pika.exceptions.AMQPConnectionError:
                print(f"Connection error during attempt {attempt}")
                time.sleep(5)
                attempt += 1

    def disconnect(self):
        try:
            self._channel.close()
            self._connection.close()
        except pika.exceptions.ChannelWrongStateError:
            print("Could not close channel.")
        except pika.exceptions.ConnectionWrongStateError:
            print("Could not close connection.")

    def tutorial_send(self):
        self._channel.basic_publish(
            exchange="automaticQaCompleted",
            routing_key="send",
            body="Hello World!"
        )

    def tutorial_register_receiver(self):
        queue = "qaRequestedAutomatedQa"
        self._channel.queue_declare(queue=queue)
        self._channel.queue_bind(queue=queue, exchange="qaRequested", routing_key="key")
        self._channel.basic_consume(
            queue=queue,
            auto_ack=True,
            on_message_callback=tutorial_callback
        )
        print("registered")

    def register_receiver(self, exchange: str, routing_key: str, callback):
        queue = f"{exchange}_{routing_key}"
        self._channel.queue_declare(queue=queue)
        self._channel.queue_bind(queue=queue, exchange=exchange, routing_key=routing_key)
        self._channel.basic_consume(
            queue=queue,
            auto_ack=True,
            on_message_callback=callback
        )


def tutorial_callback(self, channel, method, properties, body):
    print(f" [x] Received {body}")


def qa_data(channel: pika.adapters.blocking_connection.BlockingChannel, method, properties, data_id: str):
    print(f"Received data with ID {data_id} for automated review")
    print(f"Auto-accepting data with ID {data_id}") # TODO actual logic here
    message={"dataId": data_id, "qaResult": "Accepted"} # TODO use client for accepted
    channel.basic_publish(
        exchange=manual_qa_requested_exchange,
        routing_key=data_key,
        body=message.__str__()
        # properties=pika.BasicProperties()
    )


def qa_document(channel, method, properties, document_id: str):
    print(f"Received data with ID {document_id} for automated review")
    print(f"Auto-accepting data with ID {document_id}") # TODO actual logic here
    message={"dataId": document_id, "qaResult": "Accepted"} # TODO use client for accepted
    channel.basic_publish(
        exchange=manual_qa_requested_exchange,
        routing_key=data_key,
        body=message.__str__()
    )

