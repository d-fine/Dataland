package org.dataland.e2etests.utils.api

import org.dataland.e2etests.BASE_PATH_TO_USER_SERVICE
import org.dataland.userService.openApiClient.api.PortfolioControllerApi

/**
 * Utility object for accessing user service API controllers
 */
object UserService {
    val portfolioControllerApi = PortfolioControllerApi(BASE_PATH_TO_USER_SERVICE)
}
