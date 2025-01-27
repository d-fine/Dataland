package org.dataland.e2etests.utils.api

import org.dataland.datalandqaservice.openApiClient.api.AdditionalCompanyInformationDataQaReportControllerApi
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.e2etests.BASE_PATH_TO_QA_SERVICE

/**
 * Utility object for accessing backend API controllers
 */
object QaService {
    val qaControllerApi = QaControllerApi(BASE_PATH_TO_QA_SERVICE)
    val dataPointQaReportControllerApi =
        org.dataland.datalandqaservice.openApiClient.api
            .DataPointQaReportControllerApi(BASE_PATH_TO_QA_SERVICE)
    val additionalCompanyInformationDataQaReportControllerApi =
        AdditionalCompanyInformationDataQaReportControllerApi(BASE_PATH_TO_QA_SERVICE)
}
