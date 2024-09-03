package org.dataland.datalandcommunitymanager.utils
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus

/**
 * A filter class used in the searchDataRequestEntity-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class DataRequestsFilter(
    // TODO do we need to add some annotations?
    val dataTypes: Set<DataTypeEnum>?,
    val userId: String?,
    val emailAddress: String?,
    val datalandCompanyId: String?,
    val reportingPeriod: String?,
    val requestStatus: Set<RequestStatus>?,
    val accessStatus: Set<AccessStatus>?,
) {
    @get:Schema(hidden = true)
    val shouldFilterByDataTypes: Boolean
        get() = dataTypes?.isNotEmpty() ?: false

    @get:Schema(hidden = true)
    val usedDataTypesFilter: List<String>
        get() = dataTypes?.map { it.value } ?: emptyList()

    @get:Schema(hidden = true)
    val shouldFilterByUserId: Boolean
        get() = userId?.isNotEmpty() ?: false

    @get:Schema(hidden = true)
    val usedUserIdFilter: String
        get() = userId ?: ""

    @get:Schema(hidden = true)
    val shouldFilterByEmailAddress: Boolean
        get() = emailAddress?.isNotEmpty() ?: false

    @get:Schema(hidden = true)
    val shouldFilterByDatalandCompanyId: Boolean
        get() = datalandCompanyId?.isNotEmpty() ?: false

    @get:Schema(hidden = true)
    val usedDatalandCompanyIdFilter: String
        get() = datalandCompanyId ?: ""

    @get:Schema(hidden = true)
    val shouldFilterByReportingPeriod: Boolean
        get() = reportingPeriod?.isNotEmpty() ?: false

    @get:Schema(hidden = true)
    val usedReportingPeriodFilter: String
        get() = reportingPeriod ?: ""

    @get:Schema(hidden = true)
    val shouldFilterByRequestStatus: Boolean
        get() = requestStatus?.isNotEmpty() ?: false

    @get:Schema(hidden = true)
    val usedRequestStatusFilter: List<String>
        get() = requestStatus?.map { it.name } ?: emptyList()

    @get:Schema(hidden = true)
    val shouldFilterByAccessStatus: Boolean
        get() = accessStatus?.isNotEmpty() ?: false

    @get:Schema(hidden = true)
    val usedAccessStatusFilter: List<String>
        get() = accessStatus?.map { it.name } ?: emptyList()
}
