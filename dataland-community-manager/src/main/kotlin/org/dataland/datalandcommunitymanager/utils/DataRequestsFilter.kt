package org.dataland.datalandcommunitymanager.utils
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
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
    val adminComment: String?,
    val requestPriority: Set<RequestPriority>?,
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
    fun setupEmailAddressFilter(keycloakUserControllerApiService: KeycloakUserControllerApiService): List<KeycloakUserInfo> {
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

    var adminCommentMatchingSearchSubstring: Set<String>? = null

    /**
     * This function should be called when the adminComment substring filter is not empty, i.e. if shouldFilterByAdminComment
     * is true. The commentService is required to get the comments that contain the substring.
     */
    fun setUpAdminCommentFilter(
        requests: List<ExtendedStoredDataRequest>,
        searchSubstring: String?,
    ): List<ExtendedStoredDataRequest> {
        if (searchSubstring.isNullOrBlank()) return requests

        return requests.filter { request ->
            searchSubstring.let { request.adminComment?.contains(it, ignoreCase = true) } ?: false
        }
    }

    val preparedAdminCommentMatchingSearchSubstring: List<String>
        get() {
            check(!shouldFilterByAdminComment || adminCommentMatchingSearchSubstring != null) {
                "You need to call setupAdminCommentFilter(..) before querying by admin comment substring!"
            }
            return (adminCommentMatchingSearchSubstring?.toList() ?: emptyList())
        }

    val shouldFilterByRequestPriority: Boolean
        get() = requestPriority?.isNotEmpty() ?: false

    val preparedRequestPriority: List<String>
        get() = requestPriority?.map { it.name } ?: emptyList()
}
