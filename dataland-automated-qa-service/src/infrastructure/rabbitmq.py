import json
import time
import logging

import pika
import pika.exceptions
import properties as p

data_key = "data"
document_key = "document"
receiving_exchange = "itemStored"
manual_qa_requested_exchange = "manualQaRequested"
quality_assured_exchange = "dataQualityAssured"

correlation_id_header = "cloudEvents:id"
message_type_header = "cloudEvents:type"

qa_completed_type = "QA completed"
manual_qa_requested_type = "Manual QA requested"


def listen_to_message_queue():
    mq = RabbitMq(p.rabbit_mq_connection_parameters)
    mq.connect()
    mq.register_receiver(receiving_exchange, data_key, qa_data)
    mq.register_receiver(receiving_exchange, document_key, qa_document)
    # mq.register_receiver(receiving_exchange, data_key, tutorial_callback)
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
                logging.info(f"Connection attempt {attempt}")
                self._connection = pika.BlockingConnection(self._connection_parameters)
                self._channel = self._connection.channel()
                logging.info(f"Connection established")
                break
            except pika.exceptions.AMQPConnectionError:
                logging.info(f"Connection error during attempt {attempt}")
                time.sleep(5)
                attempt += 1

    def disconnect(self):
        try:
            self._channel.close()
            self._connection.close()
        except pika.exceptions.ChannelWrongStateError:
            logging.error("Could not close channel.")
        except pika.exceptions.ConnectionWrongStateError:
            logging.error("Could not close connection.")

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
            on_message_callback=callback
        )


def tutorial_callback(channel, method, properties, body):
    print(f" [x] Received {body}")


def qa_data(channel: pika.adapters.blocking_connection.BlockingChannel, method, properties: pika.BasicProperties, body: bytes):
    received_message = json.loads(body)
    bypass_qa = received_message["bypassQa"]
    data_id = received_message["dataId"]
    correlation_id = properties.headers["cloudEvents:id"]
    logging.info(f"Received data with ID {data_id} for automated review. (Correlation ID: {correlation_id})")
    if bypass_qa:
        logging.info(f"Bypassing QA for data with ID {data_id}. (Correlation ID: {correlation_id})")
        message_to_send = {
            "identifier": data_id,
            "validationResult": "Accepted"
        }  # TODO use client for accepted
        channel.basic_publish(
            exchange=quality_assured_exchange,
            routing_key=data_key,
            body=json.dumps(message_to_send),
            properties=pika.BasicProperties(
                headers={
                    correlation_id_header: correlation_id,
                    message_type_header: qa_completed_type
                }
            )
        )
    else:
        # TODO actual logic here
        logging.info(f"Auto-forwarding data with ID {data_id} to manual QA")
        channel.basic_publish(
            exchange=manual_qa_requested_exchange,
            routing_key=data_key,
            body=data_id,
            properties=pika.BasicProperties(
                headers={
                    correlation_id_header: correlation_id,
                    message_type_header: manual_qa_requested_type
                }
            )
        )
    channel.basic_ack(delivery_tag=method.delivery_tag)

def process_qa_request(channel: pika.adapters.blocking_connection.BlockingChannel, method, properties: pika.BasicProperties, body: bytes, routing_key: str):
    if routing_key == data_key:
        received_message = json.loads(body)
        bypass_qa = received_message["bypassQa"]
        resource_id = received_message["dataId"]
    else:
        bypass_qa = False
        resource_id = body
    correlation_id = properties.headers["cloudEvents:id"]
    logging.info(f"Received {routing_key} with ID {resource_id} for automated review. (Correlation ID: {correlation_id})")
    if bypass_qa:
        logging.info(f"Bypassing QA for {routing_key} with ID {resource_id}. (Correlation ID: {correlation_id})")
        message_to_send = {
            "identifier": resource_id,
            "validationResult": "Accepted"
        }  # TODO use client for accepted
        channel.basic_publish(
            exchange=quality_assured_exchange,
            routing_key=routing_key,
            body=json.dumps(message_to_send),
            properties=pika.BasicProperties(
                headers={
                    correlation_id_header: correlation_id,
                    message_type_header: qa_completed_type
                }
            )
        )
    else:
        # TODO actual logic here
        logging.info(f"Auto-forwarding data with ID {resource_id} to manual QA")
        channel.basic_publish(
            exchange=manual_qa_requested_exchange,
            routing_key=routing_key,
            body=resource_id,
            properties=pika.BasicProperties(
                headers={
                    correlation_id_header: correlation_id,
                    message_type_header: manual_qa_requested_type
                }
            )
        )
    channel.basic_ack(delivery_tag=method.delivery_tag)

def qa_document(channel, method, properties, body: str):
    pass

