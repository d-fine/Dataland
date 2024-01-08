import json
import logging
import pika.exceptions

from main.infrastructure.qa_exceptions import AutomaticQaNotPossibleError
from main.infrastructure.resources import Resource, DataResource, DocumentResource
from main.infrastructure.properties import *
from main.validation.validate import validate_data, validate_document

from dataland_backend_api_documentation_client.models.qa_status import QaStatus


def qa_data(channel, method, properties, body):
    received_message = json.loads(body)
    bypass_qa = received_message["bypassQa"]
    data_id = received_message["dataId"]
    data = DataResource(data_id)
    process_qa_request(channel, method, properties, mq_data_key, "data", bypass_qa, data, validate_data)


def qa_document(channel, method, properties, body: bytes):
    document = DocumentResource(body.decode("UTF-8"))
    process_qa_request(channel, method, properties, mq_document_key, "document", False, document, validate_document)


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
            validation_result = validate(resource, correlation_id)
            assert_status_is_valid_for_qa_completion(validation_result)
            send_qa_completed_message(channel, routing_key, resource.id, validation_result, correlation_id)
        except AutomaticQaNotPossibleError as e:
            logging.warning("ABCDEFG")
            logging.warning(f"itentifier: \"{resource.id}\" \"{type(resource.id)}\"")
            logging.warning(f"comment: \"{e.comment}\" \"{type(e.comment)}\"")
            message_to_send = {
                "identifier": resource.id,
                "comment": e.comment
            }
            channel.basic_publish(
                exchange=mq_manual_qa_requested_exchange,
                routing_key=routing_key,
                body=json.dumps(message_to_send),
                properties=pika.BasicProperties(
                    headers={
                        mq_correlation_id_header: correlation_id,
                        mq_message_type_header: mq_manual_qa_requested_type
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
        exchange=mq_quality_assured_exchange,
        routing_key=routing_key,
        body=json.dumps(message_to_send),
        properties=pika.BasicProperties(
            headers={
                mq_correlation_id_header: correlation_id,
                mq_message_type_header: mq_qa_completed_type
            }
        ),
        mandatory=True
    )


def assert_status_is_valid_for_qa_completion(status: QaStatus):
    if status != QaStatus.ACCEPTED and status != QaStatus.REJECTED:
        raise ValueError(f"Argument (status) must be in range [QaStatus.ACCEPTED, QaStatus.REJECTED]")
