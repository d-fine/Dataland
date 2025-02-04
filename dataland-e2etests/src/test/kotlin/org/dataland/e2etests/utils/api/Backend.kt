package org.dataland.e2etests.utils.api

import org.dataland.datalandbackend.openApiClient.api.AdditionalCompanyInformationDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.AssembledDatasetMigrationControllerApi
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND

/**
 * Utility object for accessing backend API controllers
 */
object Backend {
    val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val dataPointControllerApi = DataPointControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val additionalCompanyInformationDataControllerApi = AdditionalCompanyInformationDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val dataMigrationControllerApi: AssembledDatasetMigrationControllerApi =
        AssembledDatasetMigrationControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
}
