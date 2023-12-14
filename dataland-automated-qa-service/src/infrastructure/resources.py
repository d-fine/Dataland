from dataland_backend_api_documentation_client.api.company_data_controller.get_company_info import sync as get_company_info
from dataland_backend_api_documentation_client.api.meta_data_controller.get_data_meta_info import sync as get_data_meta_info
from dataland_backend_api_documentation_client.api.sme_data_controller.get_company_associated_sme_data import sync as get_sme_data
from dataland_backend_api_documentation_client.client import AuthenticatedClient
import logging
from .keycloak import KeycloakTokenManager

class Resource:
    def __init__(self, resource_id: str):
        self.id = resource_id
        self.content = None
        self._load()

    def _load(self):
        raise NotImplementedError


class DataResource(Resource):
    def _load(self):
        # TODO actually implement it
        print("TODO")
        self.whatever = None
        token = KeycloakTokenManager().get_access_token()
        client = AuthenticatedClient(
            base_url="https://local-dev.dataland.com/api",
            token=token
        )
        company_info = get_company_info(
            "73ac8cf9-1982-478b-a99f-13c9846e8346", client=client
        )
        logging.info(company_info.company_name)

        # TODO actual code below
        meta_info = get_data_meta_info(self.id, client=client)
        data = get_sme_data(self.id, client=client)
        logging.info(meta_info.data_type)
        logging.info(data.data.general.basic_information.sector)


class DocumentResource(Resource):
    def _load(self):
        # TODO actually implement it
        print("TODO")
