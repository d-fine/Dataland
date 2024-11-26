package org.dataland.datalandbackend.repositories.utils

import org.dataland.datalandbackend.model.DataType

/**
 * A filter class used in the searchDataMetaInformation()-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class NonSourceableDataSearchFilter(
    val companyId: String?,
    val dataType: DataType?,
    val reportingPeriod: String?,
    val nonSourceable: Boolean?,
) {
    val shouldFilterByCompanyId: Boolean
        get() = !companyId.isNullOrEmpty()

    val preparedCompanyId: String
        get() = companyId ?: ""

    val shouldFilterByDataType: Boolean
        get() = dataType != null

    val preparedDataType: String
        get() = dataType?.name ?: ""

    val shouldFilterByReportingPeriod: Boolean
        get() = !reportingPeriod.isNullOrEmpty()

    val preparedReportingPeriod: String
        get() = reportingPeriod ?: ""

    val shouldFilterByNonSourceable: Boolean
        get() = nonSourceable != null

    val preparedNonSourceable: Boolean?
        get() = nonSourceable
}
