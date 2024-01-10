import logging

from .resource import Resource
from ..properties import document_manager_api_url
from ..keycloak import get_access_token

from dataland_backend_api_documentation_client.client import AuthenticatedClient
from dataland_document_manager_api_documentation_client.api.document_controller.get_document import (
    sync as get_document,
)


class DocumentResource(Resource):
    """
    This class represents a document
    """

    def _load(self):
        logging.info(f"Loading document resource with ID {self.id}")
        token = get_access_token()
        documents_client = AuthenticatedClient(base_url=document_manager_api_url, token=token)
        logging.info(f"Retrieving document data with ID {self.id}")
        self.bytes = get_document(self.id, client=documents_client)
