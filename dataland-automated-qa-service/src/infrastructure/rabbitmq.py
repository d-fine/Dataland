import json
import logging

from .qa_exceptions import AutomaticQaNotPossibleError
from validation.validate import validate_data, validate_document
import pika
import pika.exceptions
from .resources import Resource, DataResource, DocumentResource
import infrastructure.properties as p

from dataland_backend_api_documentation_client.models.qa_status import QaStatus


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
    while True:
        try:
            mq = RabbitMq(p.rabbit_mq_connection_parameters)
            mq.connect()
            mq.register_receiver(receiving_exchange, data_key, qa_data)
            mq.register_receiver(receiving_exchange, document_key, qa_document)
            mq.consume_loop()
        except pika.exceptions.ConnectionClosedByBroker:
            continue
        except pika.exceptions.AMQPChannelError as err:
            logging.error(f"Caught a channel error: {err}, stopping...")
            break
        except pika.exceptions.AMQPConnectionError:
            logging.error("Connection was closed, retrying...")
            continue


class RabbitMq:
    def __init__(self, connection_parameters: pika.connection.Parameters):
        self._connection_parameters = connection_parameters
        self._connection: pika.adapters.blocking_connection.BlockingConnection | None = None
        self._channel: pika.adapters.blocking_connection.BlockingChannel | None = None

    def connect(self):
        logging.info(f"Connecting to RabbitMQ")
        self._connection = pika.BlockingConnection(self._connection_parameters)
        self._channel = self._connection.channel()
        logging.info(f"Connection established")

    def disconnect(self):
        try:
            self._channel.close()
            self._connection.close()
        except pika.exceptions.ChannelWrongStateError:
            logging.error("Could not close channel.")
        except pika.exceptions.ConnectionWrongStateError:
            logging.error("Could not close connection.")

    def register_receiver(self, exchange: str, routing_key: str, callback):
        queue = f"{exchange}_{routing_key}"
        self._channel.queue_declare(queue=queue, durable=True)
        self._channel.queue_bind(queue=queue, exchange=exchange, routing_key=routing_key)
        self._channel.basic_consume(
            queue=queue,
            on_message_callback=callback
        )

    def consume_loop(self):
        self._channel.start_consuming()


def qa_data(channel, method, properties, body):
    received_message = json.loads(body)
    bypass_qa = received_message["bypassQa"]
    data_id = received_message["dataId"]
    data = DataResource(data_id)
    process_qa_request(channel, method, properties, data_key, "data", bypass_qa, data, validate_data)


def qa_document(channel, method, properties, body):
    document = DocumentResource(body)
    process_qa_request(channel, method, properties, document_key, "document", False, document, validate_document)


def process_qa_request(
        channel: pika.adapters.blocking_connection.BlockingChannel,
        method,
        properties: pika.BasicProperties,
        routing_key: str,
        resource_type: str,
        bypass_qa: bool,
        resource: Resource,
        validate
):
    correlation_id = properties.headers["cloudEvents:id"]
    logging.info(
        f"Received {resource_type} with ID {resource.id} for automated review. (Correlation ID: {correlation_id})")
    if bypass_qa:
        logging.info(f"Bypassing QA for {resource_type} with ID {resource.id}. (Correlation ID: {correlation_id})")
        send_qa_completed_message(channel, routing_key, resource.id, QaStatus.ACCEPTED, correlation_id)
    else:
        logging.info(
            f"Evaluating {resource_type} with ID {resource.id}. (Correlation ID: {correlation_id})"
        )
        try:
            validation_result = validate(resource, correlation_id)  # TODO don't use None but the proper resource
            assert_status_is_valid_for_qa_completion(validation_result)
            send_qa_completed_message(channel, routing_key, resource.id, validation_result, correlation_id)
        except AutomaticQaNotPossibleError:
            channel.basic_publish(
                exchange=manual_qa_requested_exchange,
                routing_key=routing_key,
                body=resource.id,
                properties=pika.BasicProperties(
                    headers={
                        correlation_id_header: correlation_id,
                        message_type_header: manual_qa_requested_type
                    }
                ),
                mandatory=True
            )
    channel.basic_ack(delivery_tag=method.delivery_tag)


def send_qa_completed_message(
        channel: pika.adapters.blocking_connection.BlockingChannel,
        routing_key: str,
        resource_id: str,
        status: QaStatus,
        correlation_id: str
):
    assert_status_is_valid_for_qa_completion(status)
    message_to_send = {
        "identifier": resource_id,
        "validationResult": status
    }
    channel.basic_publish(
        exchange=quality_assured_exchange,
        routing_key=routing_key,
        body=json.dumps(message_to_send),
        properties=pika.BasicProperties(
            headers={
                correlation_id_header: correlation_id,
                message_type_header: qa_completed_type
            }
        ),
        mandatory=True
    )


def assert_status_is_valid_for_qa_completion(status: QaStatus):
    if status != QaStatus.ACCEPTED and status != QaStatus.REJECTED:
        raise ValueError(f"Argument (status) must be in range [QaStatus.ACCEPTED, QaStatus.REJECTED]")
