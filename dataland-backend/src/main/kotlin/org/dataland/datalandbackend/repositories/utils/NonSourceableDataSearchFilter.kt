package org.dataland.datalandbackend.repositories.utils

import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * A filter class used in the searchNonSourceableData()-Method which allows
 * convenient usage of SpEL  instructions in the query
 */
data class NonSourceableDataSearchFilter(
    val companyId: String?,
    val dataType: DataType?,
    val reportingPeriod: String?,
    val qaStatus: QaStatus? = null,
    val currentlyActive: Boolean? = null,
    val nonSourceable: Boolean? = null,
) {
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

    val shouldFilterByQaStatus: Boolean
        get() = qaStatus != null

    val preparedQaStatus: QaStatus?
        get() = qaStatus

    private val effectiveCurrentlyActive: Boolean?
        get() = currentlyActive ?: nonSourceable

    val shouldFilterByCurrentlyActive: Boolean
        get() = effectiveCurrentlyActive != null

    val preparedCurrentlyActive: Boolean?
        get() = effectiveCurrentlyActive

    // Backward-compatible aliases for legacy sourceability query usage.
    val shouldFilterByIsNonSourceable: Boolean
        get() = shouldFilterByCurrentlyActive

    val preparedIsNonSourceable: Boolean?
        get() = preparedCurrentlyActive
}
