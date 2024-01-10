import unittest
from unittest.mock import Mock

import main.infrastructure.resources.data_resource as data_resources
import main.infrastructure.resources.document_resource as document_resources
from main.infrastructure.resources import Resource, DataResource, DocumentResource

from dataland_backend_api_documentation_client.models.data_meta_information import (
    DataMetaInformation,
)
from dataland_backend_api_documentation_client.models.data_type_enum import DataTypeEnum
from dataland_backend_api_documentation_client.models.qa_status import QaStatus
from dataland_backend_api_documentation_client.models.company_associated_data_sme_data import (
    CompanyAssociatedDataSmeData,
)


class TestResource(Resource):
    __test__ = False

    counter = 0

    def _load(self) -> None:
        TestResource.counter += 1


def get_data_meta_info_mock(data_id: str, client) -> DataMetaInformation:
    return DataMetaInformation(
        data_id="data-id",
        company_id="company-id",
        data_type=DataTypeEnum.SME,
        upload_time=0,
        reporting_period="reporting period",
        currently_active=True,
        qa_status=QaStatus.ACCEPTED,
    )


def get_sme_data_mock(data_id: str, client) -> CompanyAssociatedDataSmeData:
    data = dict({"dummy": "exists"})
    return CompanyAssociatedDataSmeData(company_id="company-id", reporting_period="reporting period", data=data)


class ResourceTest(unittest.TestCase):
    def test_resource_is_loaded_on_creation(self) -> None:
        self.assertEqual(0, TestResource.counter)
        TestResource("resource-id")
        self.assertEqual(1, TestResource.counter)

    def test_data_is_fetched_correctly(self) -> None:
        data_resources.get_access_token = Mock()
        data_resources.get_data_meta_info = get_data_meta_info_mock
        data_resources.get_sme_data = get_sme_data_mock
        data_resource = DataResource("data-id")
        self.assertEqual("data-id", data_resource.id)
        self.assertEqual(DataTypeEnum.SME, data_resource.meta_info.data_type)
        self.assertEqual("exists", data_resource.data["dummy"])

    def test_document_is_fetched_correctly(self) -> None:
        document_resources.get_access_token = Mock()
        document_resources.get_document = lambda document_id, client: 42
        document_resource = DocumentResource("document-id")
        self.assertEqual("document-id", document_resource.id)
        self.assertEqual(42, document_resource.bytes)
