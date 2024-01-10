import logging
from typing import Self, Any, cast
from http import HTTPStatus

from dataland_backend_api_documentation_client import errors

from .resource import Resource
from ..keycloak import get_access_token
from ..properties import backend_api_url

from dataland_backend_api_documentation_client.api.meta_data_controller.get_data_meta_info import (
    sync as get_data_meta_info,
)

from dataland_backend_api_documentation_client.models.company_associated_data_eu_taxonomy_data_for_financials import (
    CompanyAssociatedDataEuTaxonomyDataForFinancials,
)
from dataland_backend_api_documentation_client.models.company_associated_data_eu_taxonomy_data_for_non_financials \
    import (
        CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
    )
from dataland_backend_api_documentation_client.models.company_associated_data_lksg_data import (
    CompanyAssociatedDataLksgData,
)
from dataland_backend_api_documentation_client.models.company_associated_data_sfdr_data import (
    CompanyAssociatedDataSfdrData,
)
from dataland_backend_api_documentation_client.models.company_associated_data_pathways_to_paris_data import (
    CompanyAssociatedDataPathwaysToParisData,
)
from dataland_backend_api_documentation_client.models.company_associated_data_sme_data import (
    CompanyAssociatedDataSmeData,
)
from dataland_backend_api_documentation_client.models.company_associated_data_gdv_data import (
    CompanyAssociatedDataGdvData,
)

from dataland_backend_api_documentation_client.client import AuthenticatedClient
from dataland_backend_api_documentation_client.models.data_meta_information import (
    DataTypeEnum,
)


class DataResource(Resource):
    """
    This class represents a dataset
    """

    def _load(self: Self) -> None:
        logging.info(f"Loading data resource with ID {self.id}")
        token = get_access_token()
        backend_client = AuthenticatedClient(base_url=backend_api_url, token=token)
        logging.info(f"Retrieving meta information for dataset with ID {self.id}")
        self.meta_info = get_data_meta_info(self.id, client=backend_client)
        logging.info(f"Retrieving dataset with ID {self.id}")
        self.data = _get_data(data_type=self.meta_info.data_type, data_id=self.id, client=backend_client)


def _get_data(data_type: DataTypeEnum, data_id: str, client: AuthenticatedClient) -> any:
    type_to_company_associated_data = {
        DataTypeEnum.EUTAXONOMY_FINANCIALS: CompanyAssociatedDataEuTaxonomyDataForFinancials,
        DataTypeEnum.EUTAXONOMY_NON_FINANCIALS: CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
        DataTypeEnum.LKSG: CompanyAssociatedDataLksgData,
        DataTypeEnum.SFDR: CompanyAssociatedDataSfdrData,
        DataTypeEnum.P2P: CompanyAssociatedDataPathwaysToParisData,
        DataTypeEnum.SME: CompanyAssociatedDataSmeData,
        DataTypeEnum.GDV: CompanyAssociatedDataGdvData,
    }
    response = client.get_httpx_client().request(method="get", url=f"/data/{data_type}/{data_id}")
    if response.status_code == HTTPStatus.OK:
        return type_to_company_associated_data.get(data_type).from_dict(response.json())
    if response.status_code == HTTPStatus.UNAUTHORIZED:
        return cast(Any, None)
    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    return None
