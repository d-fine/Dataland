package org.dataland.datalandcommunitymanager.utils
import io.swagger.v3.oas.annotations.media.Schema

/**
 * A filter class used in the searchDataRequestEntity-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class DataRequestsFilter(
    // TODO do we need to add some annotations?
    val dataType: String?,
    val userId: String?,
    val emailAddress: String?,
    val datalandCompanyId: String?,
    val reportingPeriod: String?,
    val requestStatus: String?,
    val accessStatus: String?,
) {
    @get:Schema(hidden = true)
    val shouldFilterByDataType: Boolean
        get() = dataType?.isNotEmpty() ?: false

    @get:Schema(hidden = true)
    val usedDataTypeFilter: String
        get() = dataType ?: ""

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
    val usedRequestStatusFilter: String
        get() = requestStatus ?: ""

    @get:Schema(hidden = true)
    val shouldFilterByAccessStatus: Boolean
        get() = accessStatus?.isNotEmpty() ?: false

    @get:Schema(hidden = true)
    val usedAccessStatusFilter: String
        get() = accessStatus ?: ""
}
