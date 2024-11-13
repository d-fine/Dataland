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


class AutomatedQaServiceMessage:
    """
    Class for message send to messageQueue by Automated QA Service
    """

    def __init__(self,
                 resource_id: str,
                 qa_status: QaStatus,
                 reviewer_id: str,
                 bypass_qa: bool = False,
                 comment: str = "") -> None:
        """
        Class constructor
        :param resource_id: data_id or document_id
        :param qa_status: qa_status to be assigned; nullable
        :param reviewer_id: reviewer_id
        :param bypass_qa: boolean
        :param comment: comment
        """
        self.resource_id = resource_id
        self.qa_status = qa_status
        self.reviewer_id = reviewer_id
        self.bypass_qa = bypass_qa
        self.comment = comment

    def to_dict(self) -> dict:
        """
        Returns object as dict
        :return: dict
        """
        return {**vars(self), "qa_status": self.qa_status.value}


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


def _send_persist_automated_qa_result_message(
        channel: BlockingChannel,
        routing_key: str,
        resource_id: str,
        qa_status: QaStatus,
        reviewer_id: str,
        correlation_id: str,
        bypass_qa: bool,
) -> None:
    """
    Function is used in case of bypassQA true: Automated QA Service sends message to Manual QA Service to simply store
    the QA review as 'Accepted'
    Message is sent to 'p.mq_manual_qa_requested_exchange' exchange with message type 'p.mq_persist_automated_qa_result'
    """
    message = str(AutomatedQaServiceMessage(
        resource_id=resource_id, qa_status=qa_status, reviewer_id=reviewer_id, bypass_qa=bypass_qa, ).to_dict())
    _send_message(
        channel=channel,
        exchange=p.mq_manual_qa_requested_exchange,
        routing_key=routing_key,
        message_type=p.mq_persist_automated_qa_result,
        message=message,
        correlation_id=correlation_id,
    )


def _send_automated_qa_complete_message(
        channel: BlockingChannel,
        routing_key: str,
        resource_id: str,
        qa_status: QaStatus,
        reviewer_id: str,
        correlation_id: str,
        bypass_qa: bool,
        comment: str = ""
) -> None:
    """
    Function is used in case of bypassQA false: Automated QA Service sends message to inform Manual QA Service that
    automated QA process is complete, and Manual QA process can begin.
    Message is sent to 'p.mq_manual_qa_requested_exchange' exchange with message type 'p.mq_automated_qa_complete_type'
    """
    message = str(AutomatedQaServiceMessage(
        resource_id=resource_id, qa_status=qa_status, reviewer_id=reviewer_id, bypass_qa=bypass_qa, comment=comment)
                  .to_dict())
    _send_message(
        channel=channel,
        exchange=p.mq_manual_qa_requested_exchange,
        routing_key=routing_key,
        message_type=p.mq_automated_qa_complete_type,
        message=message,
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
            channel=channel,
            routing_key=routing_key,
            resource_id=resource.id,
            qa_status=QaStatus.ACCEPTED,
            reviewer_id="automated-qa-service",
            correlation_id=correlation_id,
            bypass_qa=True,
        )
    else:
        logging.info(f"Automatic QA Service evaluating {resource_type} with ID {resource.id}."
                     f" (Correlation ID: {correlation_id})")
        try:
            validation_result = validate(resource, correlation_id)
            _assert_status_is_valid_for_qa_completion(validation_result)

            _send_automated_qa_complete_message(
                channel=channel,
                routing_key=routing_key,
                resource_id=resource.id,
                qa_status=validation_result,
                reviewer_id="automated-qa-service",
                correlation_id=correlation_id,
                bypass_qa=False
            )
        except AutomaticQaNotPossibleError as e:
            _send_automated_qa_complete_message(
                channel=channel,
                routing_key=routing_key,
                resource_id=resource.id,
                qa_status=None,
                reviewer_id="automated-qa-service",
                correlation_id=correlation_id,
                bypass_qa=False,
                comment=e.comment
            )
        channel.basic_ack(delivery_tag=method.delivery_tag)
