import logging

from typing_extensions import override

from main.infrastructure.resources import Resource
from main.infrastructure.properties import document_manager_api_url
from main.infrastructure.keycloak import get_access_token

from dataland_backend_api_documentation_client.client import AuthenticatedClient
from dataland_document_manager_api_documentation_client.api.document_controller.get_document import (
    sync as get_document,
)


class DocumentResource(Resource):
    """
    This class represents a document
    """

    @override
    def _load(self) -> None:
        logging.info(f"Loading document resource with ID {self.id}")
        token = get_access_token()
        documents_client = AuthenticatedClient(base_url=document_manager_api_url, token=token)
        logging.info(f"Retrieving document data with ID {self.id}")
        self.bytes = get_document(self.id, client=documents_client)
