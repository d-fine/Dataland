import json
import logging
from pika.adapters.blocking_connection import BlockingChannel
from pika.spec import Basic, BasicProperties
from typing import Callable


from main.infrastructure.qa_exceptions import AutomaticQaNotPossibleError
from main.infrastructure.resources import Resource, DataResource, DocumentResource, UnloadedResource
import main.infrastructure.properties as p
from main.validation.validate import validate_data, validate_document

from dataland_backend_api_documentation_client.models.qa_status import QaStatus


def qa_data(channel: BlockingChannel, method: Basic.Deliver, properties: BasicProperties, body: bytes) -> None:
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
    data = UnloadedResource(data_id) if bypass_qa else DataResource(data_id)
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


def qa_document(channel: BlockingChannel, method: Basic.Deliver, properties: BasicProperties, body: bytes) -> None:
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


def _assert_status_is_valid_for_qa_completion(status: QaStatus) -> None:
    if status not in {QaStatus.ACCEPTED, QaStatus.REJECTED}:
        raise ValueError(
            f'Argument "status" with value "{status}" must be in range [QaStatus.ACCEPTED, QaStatus.REJECTED]'
        )


def _send_message(
    channel: BlockingChannel,
    exchange: str,
    routing_key: str,
    message_type: str,
    message: dict[str, str | QaStatus],
    correlation_id: str,
) -> None:
    channel.basic_publish(
        exchange=exchange,
        routing_key=routing_key,
        body=json.dumps(message).encode("UTF-8"),
        properties=BasicProperties(
            headers={
                p.mq_correlation_id_header: correlation_id,
                p.mq_message_type_header: message_type,
            }
        ),
        mandatory=True,
    )


def _send_qa_status_changed_message(
    channel: BlockingChannel,
    routing_key: str,
    resource_id: str,
    status: QaStatus,
    correlation_id: str,
) -> None:
    message_to_send = {"identifier": resource_id, "validationResult": status, "reviewerId": "automated-qa-service"}
    _send_message(
        channel=channel,
        exchange=p.mq_quality_assured_exchange,
        routing_key=routing_key,
        message_type=p.mq_qa_status_changed_type,
        message=message_to_send,
        correlation_id=correlation_id,
    )


def _send_persist_automated_qa_result_message(
    channel: BlockingChannel,
    resource_type: str,
    data_id: str,
    status: QaStatus,
    correlation_id: str,
    reviewer_id: str,
) -> None:
    message_to_send = {"identifier": data_id, "validationResult": status,
        "reviewerId": reviewer_id, "resourceType": resource_type}
    _send_message(
        channel=channel,
        exchange=p.mq_manual_qa_requested_exchange,
        routing_key=p.mq_persist_automated_qa_result_key,
        message_type=p.mq_persist_automated_qa_result,
        message=message_to_send,
        correlation_id=correlation_id,
    )


def process_qa_request(
    channel: BlockingChannel,
    method: Basic.Deliver,
    properties: BasicProperties,
    routing_key: str,
    resource_type: str,
    bypass_qa: bool,
    resource: Resource,
    validate: Callable[[Resource, str], QaStatus],
) -> None:
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
        _send_persist_automated_qa_result_message(
            channel,
            resource_type,
            resource.id,
            QaStatus.ACCEPTED,
            correlation_id,
            "bypass-qa",
        )
        _send_qa_status_changed_message(channel, routing_key, resource.id, QaStatus.ACCEPTED, correlation_id)
    else:
        logging.info(f"Evaluating {resource_type} with ID {resource.id}. (Correlation ID: {correlation_id})")
        try:
            validation_result = validate(resource, correlation_id)
            _assert_status_is_valid_for_qa_completion(validation_result)
            _send_persist_automated_qa_result_message(
                channel,
                resource_type,
                resource.id,
                validation_result,
                correlation_id,
                "automated-qa-service",
            )
            _send_qa_status_changed_message(channel, routing_key, resource.id, validation_result, correlation_id)
        except AutomaticQaNotPossibleError as e:
            message_to_send = {"identifier": resource.id, "comment": e.comment}
            _send_message(
                channel=channel,
                exchange=p.mq_manual_qa_requested_exchange,
                routing_key=routing_key,
                message_type=p.mq_manual_qa_requested_type,
                message=message_to_send,
                correlation_id=correlation_id,
            )
    channel.basic_ack(delivery_tag=method.delivery_tag)
