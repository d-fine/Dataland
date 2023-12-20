import unittest
import unittest.mock as mock
from infrastructure.messaging import process_qa_request


class MessageProcessingTest(unittest.TestCase):
    def test_something(self):
        mock
        process_qa_request(
            channel=channel,
            method=method,
            routing_key=routing_key,
            bypass_qa=False,
            resource=resource,
            resource_type=resource_type,
            properties=properties,
            validate=lambda: True,
        )
        print("HELLO")
        self.assertTrue(False)

