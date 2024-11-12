import json
from typing import Callable

import unittest
from unittest.mock import Mock
from main.infrastructure.messaging import process_qa_request
from main.infrastructure.resources import Resource
import main.infrastructure.properties as p
from dataland_backend_api_documentation_client.models.qa_status import QaStatus
from main.infrastructure.qa_exceptions import AutomaticQaNotPossibleError


def mock_validate_raise_automated_qa_not_possible_error(resource: Resource, correlation_id: str) -> QaStatus:  # noqa: ARG001
    raise AutomaticQaNotPossibleError("Test")


def mock_properties() -> Mock:
    properties_mock = Mock()
    properties_mock.headers = {"cloudEvents:id": "dummy-correlation-id"}
    return properties_mock


def mock_resource() -> Mock:
    resource_mock = Mock()
    resource_mock.id = "dummy-id"
    return resource_mock


def build_qa_status_changed_message_body(qa_result: QaStatus) -> bytes:
    message = {"identifier": "dummy-id", "validationResult": qa_result, "reviewerId": "automated-qa-service"}
    return json.dumps(message).encode("UTF-8")


qa_forwarded_message_body = json.dumps({"identifier": "dummy-id", "comment": "Test"}).encode("UTF-8")


class MessageProcessingTest(unittest.TestCase):
    def test_should_send_accepted_message_when_qa_should_be_bypassed(self) -> None:
        self.validate_process_qa_request(
            p.mq_data_key,
            True,
            p.mq_quality_assured_exchange,
            p.mq_qa_status_changed_type,
            build_qa_status_changed_message_body(QaStatus.ACCEPTED),
            lambda resource, correlation_id: QaStatus.REJECTED,  # noqa: ARG005
        )

    def test_should_send_qa_requested_message_when_automated_qa_not_possible(self) -> None:
        self.validate_process_qa_request(
            p.mq_data_key,
            False,
            p.mq_manual_qa_requested_exchange,
            p.mq_manual_qa_requested_type,
            qa_forwarded_message_body,
            mock_validate_raise_automated_qa_not_possible_error,
        )

    def test_should_send_accepted_message_when_validation_accepts(self) -> None:
        self.validate_process_qa_request(
            p.mq_data_key,
            False,
            p.mq_quality_assured_exchange,
            p.mq_qa_status_changed_type,
            build_qa_status_changed_message_body(QaStatus.ACCEPTED),
            lambda resource, correlation_id: QaStatus.ACCEPTED,  # noqa: ARG005
        )

    def test_should_send_rejected_message_when_validation_rejects(self) -> None:
        self.validate_process_qa_request(
            p.mq_data_key,
            False,
            p.mq_quality_assured_exchange,
            p.mq_qa_status_changed_type,
            build_qa_status_changed_message_body(QaStatus.REJECTED),
            lambda resource, correlation_id: QaStatus.REJECTED,  # noqa: ARG005
        )

    def validate_process_qa_request(
        self,
        routing_key: str,
        bypass_qa: bool,
        expected_exchange: str,
        expected_message_type: str,
        expected_message_body: bytes,
        validate: Callable[[Resource, str], QaStatus],
    ) -> None:
        channel_mock = Mock()
        process_qa_request(
            channel=channel_mock,
            method=Mock(),
            properties=mock_properties(),
            routing_key=routing_key,
            resource_type=routing_key,
            bypass_qa=bypass_qa,
            resource=mock_resource(),
            validate=validate,
        )
        arguments = channel_mock.basic_publish.call_args[1]
        self.assertEqual(expected_exchange, arguments["exchange"])
        self.assertEqual(p.mq_data_key, arguments["routing_key"])
        self.assertEqual(expected_message_body, arguments["body"])
        self.assertTrue(arguments["mandatory"])
        self.assertEqual(
            "dummy-correlation-id",
            arguments["properties"].headers[p.mq_correlation_id_header],
        )
        self.assertEqual(
            expected_message_type,
            arguments["properties"].headers[p.mq_message_type_header],
        )
