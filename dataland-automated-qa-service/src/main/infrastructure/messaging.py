import json
import logging
import pika.exceptions

from main.infrastructure.qa_exceptions import AutomaticQaNotPossibleError
from main.infrastructure.resources import Resource, DataResource, DocumentResource
import main.infrastructure.properties as p
from main.validation.validate import validate_data, validate_document

from dataland_backend_api_documentation_client.models.qa_status import QaStatus


def qa_data(channel, method, properties, body):
    """
    Handler for data stored messages

    :param channel: the channel on which the message was received
    :param method: the delivery method
    :param properties: message properties
    :param body: the message body
    """
    received_message = json.loads(body)
    bypass_qa = received_message["bypassQa"]
    data_id = received_message["dataId"]
    data = DataResource(data_id)
    process_qa_request(
        channel,
        method,
        properties,
        p.mq_data_key,
        "data",
        bypass_qa,
        data,
        validate_data,
    )


def qa_document(channel, method, properties, body: bytes):
    """
    Handler for document stored messages

    :param channel: the channel on which the message was received
    :param method: the delivery method
    :param properties: message properties
    :param body: the message body
    """
    document = DocumentResource(body.decode("UTF-8"))
    process_qa_request(
        channel,
        method,
        properties,
        p.mq_document_key,
        "document",
        False,
        document,
        validate_document,
    )


def _assert_status_is_valid_for_qa_completion(status: QaStatus):
    if status != QaStatus.ACCEPTED and status != QaStatus.REJECTED:
        raise ValueError(
            f'Argument "status" with value "{status}" must be in range [QaStatus.ACCEPTED, QaStatus.REJECTED]'
        )


def _send_qa_completed_message(
    channel: pika.adapters.blocking_connection.BlockingChannel,
    routing_key: str,
    resource_id: str,
    status: QaStatus,
    correlation_id: str,
):
    _assert_status_is_valid_for_qa_completion(status)
    message_to_send = {"identifier": resource_id, "validationResult": status}
    channel.basic_publish(
        exchange=p.mq_quality_assured_exchange,
        routing_key=routing_key,
        body=json.dumps(message_to_send).encode("UTF-8"),
        properties=pika.BasicProperties(
            headers={
                p.mq_correlation_id_header: correlation_id,
                p.mq_message_type_header: p.mq_qa_completed_type,
            }
        ),
        mandatory=True,
    )


def process_qa_request(
    channel: pika.adapters.blocking_connection.BlockingChannel,
    method,
    properties: pika.BasicProperties,
    routing_key: str,
    resource_type: str,
    bypass_qa: bool,
    resource: Resource,
    validate,
):
    """
    This method is a wrapper for the validation.
    Messages on how other services should proceed on the to be reviewed resource are sent.

    :param channel: the channel on which the message was received
    :param method: the delivery method
    :param properties: message properties
    :param routing_key: the routing key of the message
    :param resource_type: the type of resource to be processed here
    :param bypass_qa: True iff no review should be performed at all and the resource should just be accepted
    :param resource: the resource to be reviewed
    :param validate: the validation function to call on the resource
    """
    correlation_id = properties.headers["cloudEvents:id"]
    logging.info(
        f"Received {resource_type} with ID {resource.id} for automated review. (Correlation ID: {correlation_id})"
    )
    if bypass_qa:
        logging.info(f"Bypassing QA for {resource_type} with ID {resource.id}. (Correlation ID: {correlation_id})")
        _send_qa_completed_message(channel, routing_key, resource.id, QaStatus.ACCEPTED, correlation_id)
    else:
        logging.info(f"Evaluating {resource_type} with ID {resource.id}. (Correlation ID: {correlation_id})")
        try:
            validation_result = validate(resource, correlation_id)
            _assert_status_is_valid_for_qa_completion(validation_result)
            _send_qa_completed_message(channel, routing_key, resource.id, validation_result, correlation_id)
        except AutomaticQaNotPossibleError as e:
            logging.warning("ABCDEFG")
            logging.warning(f'itentifier: "{resource.id}" "{type(resource.id)}"')
            logging.warning(f'comment: "{e.comment}" "{type(e.comment)}"')
            message_to_send = {"identifier": resource.id, "comment": e.comment}
            channel.basic_publish(
                exchange=p.mq_manual_qa_requested_exchange,
                routing_key=routing_key,
                body=json.dumps(message_to_send).encode("UTF-8"),
                properties=pika.BasicProperties(
                    headers={
                        p.mq_correlation_id_header: correlation_id,
                        p.mq_message_type_header: p.mq_manual_qa_requested_type,
                    }
                ),
                mandatory=True,
            )
    channel.basic_ack(delivery_tag=method.delivery_tag)
