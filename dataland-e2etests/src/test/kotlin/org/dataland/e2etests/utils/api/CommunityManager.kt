package org.dataland.e2etests.utils.api

import org.dataland.communitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.e2etests.BASE_PATH_TO_COMMUNITY_MANAGER

/**
 * Utility object for accessing community manager API controllers
 */
object CommunityManager {
    val companyRolesControllerApi = CompanyRolesControllerApi(BASE_PATH_TO_COMMUNITY_MANAGER)
}
