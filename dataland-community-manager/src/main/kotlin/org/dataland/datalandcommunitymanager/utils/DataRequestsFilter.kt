package org.dataland.datalandcommunitymanager.utils
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus

/**
 * A filter class used in the searchDataRequestEntity-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class DataRequestsFilter(
    val dataType: Set<DataTypeEnum>? = null,
    val userId: String? = null,
    val emailAddress: String? = null,
    val datalandCompanyId: String? = null,
    val reportingPeriod: String? = null,
    val requestStatus: Set<RequestStatus>? = null,
    val accessStatus: Set<AccessStatus>? = null,
    val adminComment: String? = null,
    val requestPriority: Set<RequestPriority>? = null,
) {
    val shouldFilterByDataType: Boolean
        get() = dataType?.isNotEmpty() ?: false

    val preparedDataType: List<String>
        get() = dataType?.map { it.value } ?: emptyList()

    val shouldFilterByUserId: Boolean
        get() = userId?.isNotEmpty() ?: false

    val preparedUserId: String
        get() = userId ?: ""

    val shouldFilterByEmailAddress: Boolean
        get() = emailAddress?.isNotEmpty() ?: false

    var userIdsMatchingEmailAddress: Set<String>? = null

    /**
     * This function should be called when the email address filter is not empty, i.e. if shouldFilterByEmailAddress
     * is true. The keycloakUserControllerApiService is required to get the user ids for the email addresses.
     */
    fun setupEmailAddressFilter(keycloakUserControllerApiService: KeycloakUserService): List<KeycloakUserInfo> {
        val userInfoList =
            emailAddress
                ?.takeIf { shouldFilterByEmailAddress }
                ?.let { keycloakUserControllerApiService.searchUsers(it) }

        userIdsMatchingEmailAddress = userInfoList?.map { it.userId }?.toSet()

        return userInfoList ?: emptyList()
    }

    val preparedUserIdsMatchingEmailAddress: List<String>
        get() {
            check(!shouldFilterByEmailAddress || userIdsMatchingEmailAddress != null) {
                "You need to call setupEmailAddressFilter(..) before querying by email address!"
            }
            return userIdsMatchingEmailAddress?.toList() ?: emptyList()
        }

    val shouldFilterByDatalandCompanyId: Boolean
        get() = datalandCompanyId?.isNotEmpty() ?: false

    val preparedDatalandCompanyId: String
        get() = datalandCompanyId ?: ""

    val shouldFilterByReportingPeriod: Boolean
        get() = reportingPeriod?.isNotEmpty() ?: false

    val preparedReportingPeriod: String
        get() = reportingPeriod ?: ""

    val shouldFilterByRequestStatus: Boolean
        get() = requestStatus?.isNotEmpty() ?: false

    val preparedRequestStatus: List<String>
        get() = requestStatus?.map { it.name } ?: emptyList()

    val shouldFilterByAccessStatus: Boolean
        get() = accessStatus?.isNotEmpty() ?: false

    val preparedAccessStatus: List<String>
        get() = accessStatus?.map { it.name } ?: emptyList()

    val shouldFilterByAdminComment: Boolean
        get() = adminComment?.isNotEmpty() ?: false

    val preparedAdminCommentMatchingSearchSubstring: String
        get() = adminComment ?: ""

    val shouldFilterByRequestPriority: Boolean
        get() = requestPriority?.isNotEmpty() ?: false

    val preparedRequestPriority: List<String>
        get() = requestPriority?.map { it.name } ?: emptyList()
}
