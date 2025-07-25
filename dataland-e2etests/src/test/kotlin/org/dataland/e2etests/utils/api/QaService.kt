package org.dataland.e2etests.utils.api

import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.datalandqaservice.openApiClient.api.SfdrDataQaReportControllerApi
import org.dataland.e2etests.BASE_PATH_TO_QA_SERVICE

/**
 * Utility object for accessing backend API controllers
 */
object QaService {
    val qaControllerApi = QaControllerApi(BASE_PATH_TO_QA_SERVICE)
    val dataPointQaReportControllerApi =
        org.dataland.datalandqaservice.openApiClient.api
            .DataPointQaReportControllerApi(BASE_PATH_TO_QA_SERVICE)
    val assembledDataMigrationControllerApi =
        org.dataland.datalandqaservice.openApiClient.api
            .AssembledDataMigrationControllerApi(BASE_PATH_TO_QA_SERVICE)
    val sfdrDataQaReportControllerApi = SfdrDataQaReportControllerApi(BASE_PATH_TO_QA_SERVICE)
}
