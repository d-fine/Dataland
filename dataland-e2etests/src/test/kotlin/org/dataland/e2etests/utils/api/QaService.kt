package org.dataland.e2etests.utils.api

import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.e2etests.BASE_PATH_TO_QA_SERVICE

/**
 * Utility object for accessing backend API controllers
 */
object QaService {
    val QaControllerApi = QaControllerApi(BASE_PATH_TO_QA_SERVICE)
}
