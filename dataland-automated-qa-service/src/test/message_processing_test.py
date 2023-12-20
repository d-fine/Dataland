import logging
import pika
import unittest
from unittest.mock import Mock, MagicMock
from main.infrastructure.messaging import process_qa_request
import main.infrastructure.properties as p
from dataland_backend_api_documentation_client.models.qa_status import QaStatus
from main.infrastructure.qa_exceptions import AutomaticQaNotPossibleError


def mock_validate_raise_automated_qa_not_possible_error(resource, correlation_id):
    raise AutomaticQaNotPossibleError("Test")
    # return QaStatus.ACCEPTED


def mock_basic_publish(
        exchange: str,
        routing_key: str,
        body: str,
        properties: pika.BasicProperties,
        mandatory: bool,
):
    print("this worked")


class MessageProcessingTest(unittest.TestCase):
    def test_working(self):
        print("START")
        mock_channel = MagicMock()
        mock_channel.basic_publish = mock_basic_publish
        mock_method = Mock()
        mock_properties = Mock()
        mock_properties.headers = {"cloudEvents:id": "dummy"}
        mock_resource = Mock()
        process_qa_request(
            channel=mock_channel,
            method=mock_method,
            properties=mock_properties,
            routing_key=p.mq_data_key,
            resource_type="data",
            bypass_qa=False,
            resource=mock_resource,
            validate=mock_validate_raise_automated_qa_not_possible_error,
        )
        print("END")

    def test_failing(self):
        print("START")
        mock_channel = MagicMock()
        mock_channel.basic_publish = mock_basic_publish
        mock_method = Mock()
        mock_properties = Mock()
        mock_properties.headers = {"cloudEvents:id": "dummy"}
        mock_resource = Mock()
        process_qa_request(
            channel=mock_channel,
            method=mock_method,
            properties=mock_properties,
            routing_key=p.mq_data_key,
            resource_type="data",
            bypass_qa=False,
            resource=mock_resource,
            validate=mock_validate_raise_automated_qa_not_possible_error,
        )
        print("END")
        self.assertTrue(False)

