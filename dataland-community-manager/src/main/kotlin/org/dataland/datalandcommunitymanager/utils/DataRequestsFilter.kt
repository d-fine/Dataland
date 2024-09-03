package org.dataland.datalandcommunitymanager.utils
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus

/**
 * A filter class used in the searchDataRequestEntity-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class DataRequestsFilter(
    val dataType: Set<DataTypeEnum>?,
    val userId: String?,
    val userIdsFromEmailAddress: Set<String>?,
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
        get() = userIdsFromEmailAddress != null

    val usedEmailAddressFilter: List<String>
        get() = userIdsFromEmailAddress?.toList() ?: emptyList()

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
