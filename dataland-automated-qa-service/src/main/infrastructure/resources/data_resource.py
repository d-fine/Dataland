import logging
from collections.abc import Callable

from .resource import Resource
from infrastructure.keycloak import get_access_token
from infrastructure.properties import backend_api_url

from dataland_backend_api_documentation_client.api.meta_data_controller.get_data_meta_info import sync as get_data_meta_info
from dataland_backend_api_documentation_client.api.eu_taxonomy_data_for_financials_controller.get_company_associated_eu_taxonomy_data_for_financials import sync as get_eu_taxonomy_financials_data
from dataland_backend_api_documentation_client.api.eu_taxonomy_data_for_non_financials_controller.get_company_associated_eu_taxonomy_data_for_non_financials import sync as get_eu_taxonomy_non_financials_data
from dataland_backend_api_documentation_client.api.lksg_data_controller.get_company_associated_lksg_data import sync as get_lksg_data
from dataland_backend_api_documentation_client.api.sfdr_data_controller.get_company_associated_sfdr_data import sync as get_sfdr_data
from dataland_backend_api_documentation_client.api.p_2p_data_controller.get_company_associated_p2_p_data import sync as get_p2p_data
from dataland_backend_api_documentation_client.api.sme_data_controller.get_company_associated_sme_data import sync as get_sme_data
from dataland_backend_api_documentation_client.client import AuthenticatedClient
from dataland_backend_api_documentation_client.models.data_meta_information import DataTypeEnum


class DataResource(Resource):
    def _load(self):
        logging.info(f"Loading data resource with ID {self.id}")
        token = get_access_token()
        backend_client = AuthenticatedClient(
            base_url=backend_api_url,
            token=token
        )
        logging.info(f"Retrieving meta information for dataset with ID {self.id}")
        self.meta_info = get_data_meta_info(self.id, client=backend_client)
        logging.info(f"Retrieving dataset with ID {self.id}")
        retrieve_data = _get_data_retrieval_method(self.meta_info.data_type)
        self.data = retrieve_data(self.id, backend_client)


def _get_data_retrieval_method(data_type: DataTypeEnum) -> Callable[[str, AuthenticatedClient], any]:
    if data_type == DataTypeEnum.EUTAXONOMY_FINANCIALS:
        return lambda data_id, client: get_eu_taxonomy_financials_data(data_id, client=client)
    elif data_type == DataTypeEnum.EUTAXONOMY_NON_FINANCIALS:
        return lambda data_id, client: get_eu_taxonomy_non_financials_data(data_id, client=client)
    elif data_type == DataTypeEnum.LKSG:
        return lambda data_id, client: get_lksg_data(data_id, client=client)
    elif data_type == DataTypeEnum.SFDR:
        return lambda data_id, client: get_sfdr_data(data_id, client=client)
    elif data_type == DataTypeEnum.P2P:
        return lambda data_id, client: get_p2p_data(data_id, client=client)
    elif data_type == DataTypeEnum.SME:
        return lambda data_id, client: get_sme_data(data_id, client=client)
    else:
        raise ValueError(f"No client specified for data type {data_type}")
