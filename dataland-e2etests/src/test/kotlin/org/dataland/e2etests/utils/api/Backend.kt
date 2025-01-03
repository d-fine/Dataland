package org.dataland.e2etests.utils.api

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND

/**
 * Utility object for accessing backend API controllers
 */
object Backend {
    val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    val dataPointControllerApi = DataPointControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
}
