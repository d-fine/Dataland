import json

import unittest
from unittest.mock import Mock
from main.infrastructure.messaging import process_qa_request
import main.infrastructure.properties as p
from dataland_backend_api_documentation_client.models.qa_status import QaStatus
from main.infrastructure.qa_exceptions import AutomaticQaNotPossibleError


def mock_validate_raise_automated_qa_not_possible_error(resource, correlation_id):
    raise AutomaticQaNotPossibleError("Test")


def mock_properties():
    properties_mock = Mock()
    properties_mock.headers = {"cloudEvents:id": "dummy-correlation-id"}
    return properties_mock


def mock_resource():
    resource_mock = Mock()
    resource_mock.id = "dummy-id"
    return resource_mock


def build_qa_completed_message_body(qa_result: QaStatus) -> str:
    return json.dumps({
        "identifier": "dummy-id",
        "validationResult": qa_result
    })


qa_forwarded_message_body = json.dumps({
    "identifier": "dummy-id",
    "validationResult": "Test"
})


class MessageProcessingTest(unittest.TestCase):
    def test_should_send_accepted_message_when_qa_should_be_bypassed(self):
        self.validate_process_qa_request(
            p.mq_data_key,
            True,
            p.mq_quality_assured_exchange,
            p.mq_qa_completed_type,
            build_qa_completed_message_body(QaStatus.ACCEPTED),
            lambda resource, correlation_id: QaStatus.REJECTED
        )

    def test_should_send_qa_requested_message_when_automated_qa_not_possible(self):
        self.validate_process_qa_request(
            p.mq_data_key,
            False,
            p.mq_manual_qa_requested_exchange,
            p.mq_manual_qa_requested_type,
            qa_forwarded_message_body,
            mock_validate_raise_automated_qa_not_possible_error
        )

    def test_should_send_accepted_message_when_validation_accepts(self):
        self.validate_process_qa_request(
            p.mq_data_key,
            False,
            p.mq_quality_assured_exchange,
            p.mq_qa_completed_type,
            build_qa_completed_message_body(QaStatus.ACCEPTED),
            lambda resource, correlation_id: QaStatus.ACCEPTED
        )

    def test_should_send_rejected_message_when_validation_rejects(self):
        self.validate_process_qa_request(
            p.mq_data_key,
            False,
            p.mq_quality_assured_exchange,
            p.mq_qa_completed_type,
            build_qa_completed_message_body(QaStatus.REJECTED),
            lambda resource, correlation_id: QaStatus.REJECTED
        )

    def validate_process_qa_request(
            self,
            routing_key: str,
            bypass_qa: bool,
            expected_exchange: str,
            expected_message_type: str,
            expected_message_body: str,
            validate
    ):
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
        self.assertEqual(True, arguments["mandatory"])
        self.assertEqual("dummy-correlation-id", arguments["properties"].headers[p.mq_correlation_id_header])
        self.assertEqual(expected_message_type, arguments["properties"].headers[p.mq_message_type_header])
