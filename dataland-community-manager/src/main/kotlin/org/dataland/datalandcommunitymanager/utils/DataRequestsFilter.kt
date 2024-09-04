package org.dataland.datalandcommunitymanager.utils
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.services.KeycloakUserControllerApiService

/**
 * A filter class used in the searchDataRequestEntity-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class DataRequestsFilter(
    val dataType: Set<DataTypeEnum>?,
    val userId: String?,
    val emailAddress: String?,
    val datalandCompanyId: String?,
    val reportingPeriod: String?,
    val requestStatus: Set<RequestStatus>?,
    val accessStatus: Set<AccessStatus>?,
) {
    val shouldFilterByDataType: Boolean
        get() = dataType?.isNotEmpty() ?: false

    val usedDataTypeFilter: List<String>
        get() = dataType?.map { it.value } ?: emptyList()

    val shouldFilterByUserId: Boolean
        get() = userId?.isNotEmpty() ?: false

    val usedUserIdFilter: String
        get() = userId ?: ""

    val shouldFilterByEmailAddress: Boolean
        get() = emailAddress?.isNotEmpty() ?: false

    var userIdsFromEmailAddress: Set<String>? = null

    /**
     * This function should be called when the email address filter is not empty, i.e. if shouldFilterByEmailAddress
     * is true. The keycloakUserControllerApiService is required to get the user ids for the email addresses.
     */
    fun setupEmailFilter(keycloakUserControllerApiService: KeycloakUserControllerApiService): List<KeycloakUserInfo> {
        val userInfoList = emailAddress
            ?.takeIf { shouldFilterByEmailAddress }
            ?.let { keycloakUserControllerApiService.searchUsers(it) }

        userIdsFromEmailAddress = userInfoList?.map { it.userId }?.toSet()

        return userInfoList ?: emptyList()
    }

    val usedEmailAddressFilter: List<String>
        get() {
            check(!shouldFilterByEmailAddress || userIdsFromEmailAddress != null) {
                "You need to call setupEmailFilter(..) before querying by email address!"
            }
            return userIdsFromEmailAddress?.toList() ?: emptyList()
        }

    val shouldFilterByDatalandCompanyId: Boolean
        get() = datalandCompanyId?.isNotEmpty() ?: false

    val usedDatalandCompanyIdFilter: String
        get() = datalandCompanyId ?: ""

    val shouldFilterByReportingPeriod: Boolean
        get() = reportingPeriod?.isNotEmpty() ?: false

    val usedReportingPeriodFilter: String
        get() = reportingPeriod ?: ""

    val shouldFilterByRequestStatus: Boolean
        get() = requestStatus?.isNotEmpty() ?: false

    val usedRequestStatusFilter: List<String>
        get() = requestStatus?.map { it.name } ?: emptyList()

    val shouldFilterByAccessStatus: Boolean
        get() = accessStatus?.isNotEmpty() ?: false

    val usedAccessStatusFilter: List<String>
        get() = accessStatus?.map { it.name } ?: emptyList()
}
