package org.dataland.datalandbackend.repositories.utils

import org.dataland.datalandbackend.model.DataType
import java.util.UUID

/**
 * A filter class used in the searchDataMetaInformation()-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class NonSourceableDataSearchFilter(
    val eventId: UUID?,
    val companyId: String?,
    val dataType: DataType?,
    val reportingPeriod: String?,
    val isNonSourceable: Boolean?,
) {
    val shouldFilterByEventId: Boolean
        get() = eventId != null

    val preparedEventId: UUID?
        get() = eventId
    val shouldFilterByCompanyId: Boolean
        get() = !companyId.isNullOrEmpty()

    val preparedCompanyId: String
        get() = companyId ?: ""

    val shouldFilterByDataType: Boolean
        get() = dataType != null

    val preparedDataType: DataType?
        get() = dataType

    val shouldFilterByReportingPeriod: Boolean
        get() = !reportingPeriod.isNullOrEmpty()

    val preparedReportingPeriod: String
        get() = reportingPeriod ?: ""

    val shouldFilterByIsNonSourceable: Boolean
        get() = isNonSourceable != null

    val preparedIsNonSourceable: Boolean?
        get() = isNonSourceable
}
