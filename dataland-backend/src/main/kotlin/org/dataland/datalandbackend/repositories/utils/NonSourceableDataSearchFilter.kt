package org.dataland.datalandbackend.repositories.utils

import org.dataland.datalandbackend.model.DataType

/**
 * A filter class used in the searchNonSourceableData()-Method which allows
 * convenient usage of SpEL  instructions in the query
 */
data class NonSourceableDataSearchFilter(
    val companyId: String? = null,
    val dataType: DataType? = null,
    val reportingPeriod: String? = null,
    val isNonSourceable: Boolean? = null,
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

    val shouldFilterByIsNonSourceable: Boolean
        get() = isNonSourceable != null

    val preparedIsNonSourceable: Boolean?
        get() = isNonSourceable
}
