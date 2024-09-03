import logging
from typing import Any, cast
from http import HTTPStatus

from dataland_backend_api_documentation_client import errors
from typing_extensions import override

from main.infrastructure.resources import Resource
from main.infrastructure.keycloak import get_access_token
from main.infrastructure.properties import backend_api_url

from dataland_backend_api_documentation_client.api.meta_data_controller.get_data_meta_info import (
    sync as get_data_meta_info,
)

from dataland_backend_api_documentation_client.models.company_associated_data_eu_taxonomy_data_for_financials import (
    CompanyAssociatedDataEuTaxonomyDataForFinancials,
)
from dataland_backend_api_documentation_client.models.company_associated_data_eutaxonomy_non_financials_data import (
    CompanyAssociatedDataEutaxonomyNonFinancialsData,
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
from dataland_backend_api_documentation_client.models.company_associated_data_vsme_data import (
    CompanyAssociatedDataVsmeData,
)
from dataland_backend_api_documentation_client.models.company_associated_data_esg_questionnaire_data import (
    CompanyAssociatedDataEsgQuestionnaireData,
)
from dataland_backend_api_documentation_client.models.company_associated_data_heimathafen_data import (
    CompanyAssociatedDataHeimathafenData,
)
from dataland_backend_api_documentation_client.models.company_associated_data_additional_company_information_data \
    import (CompanyAssociatedDataAdditionalCompanyInformationData)

from dataland_backend_api_documentation_client.client import AuthenticatedClient
from dataland_backend_api_documentation_client.models.data_meta_information import (
    DataTypeEnum,
)


class DataResource(Resource):
    """
    This class represents a dataset
    """

    @override
    def _load(self) -> None:
        logging.info(f"Loading data resource with ID {self.id}")
        token = get_access_token()
        backend_client = AuthenticatedClient(base_url=backend_api_url, token=token)
        logging.info(f"Retrieving meta information for dataset with ID {self.id}")
        self.meta_info = get_data_meta_info(self.id, client=backend_client)
        logging.info(f"Retrieving dataset with ID {self.id}")
        self.data = _get_data(data_type=self.meta_info.data_type, data_id=self.id, client=backend_client).data


def _get_data(data_type: DataTypeEnum, data_id: str, client: AuthenticatedClient) -> any:
    type_to_company_associated_data = {
        DataTypeEnum.EUTAXONOMY_FINANCIALS: CompanyAssociatedDataEuTaxonomyDataForFinancials,
        DataTypeEnum.EUTAXONOMY_NON_FINANCIALS: CompanyAssociatedDataEutaxonomyNonFinancialsData,
        DataTypeEnum.LKSG: CompanyAssociatedDataLksgData,
        DataTypeEnum.SFDR: CompanyAssociatedDataSfdrData,
        DataTypeEnum.P2P: CompanyAssociatedDataPathwaysToParisData,
        DataTypeEnum.VSME: CompanyAssociatedDataVsmeData,
        DataTypeEnum.ESG_QUESTIONNAIRE: CompanyAssociatedDataEsgQuestionnaireData,
        DataTypeEnum.HEIMATHAFEN: CompanyAssociatedDataHeimathafenData,
        DataTypeEnum.ADDITIONAL_COMPANY_INFORMATION: CompanyAssociatedDataAdditionalCompanyInformationData
    }
    response = client.get_httpx_client().request(method="get", url=f"/data/{data_type}/{data_id}")
    if response.status_code == HTTPStatus.OK:
        return type_to_company_associated_data.get(data_type).from_dict(response.json())
    if response.status_code == HTTPStatus.UNAUTHORIZED:
        return cast(Any, None)
    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    return None
