package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * A filter class used in the searching for unreviewed datasets which allows
 * convenient usage of SEPL instructions in the query
 */
data class QaSearchFilter(
    val dataTypes: Set<DataTypeEnum>? = null,
    val reportingPeriods: Set<String>? = null,
    val companyIds: Set<String>? = null,
    val companyName: String? = null,
    val qaStatuses: Set<QaStatus>? = null,
) {
    val shouldFilterByDataType: Boolean
        get() = dataTypes?.isNotEmpty() ?: false

    val preparedDataTypes: List<String>
        get() = dataTypes?.map { it.value } ?: emptyList()

    val shouldFilterByCompanyId: Boolean
        get() = companyIds?.isNotEmpty() ?: false

    val preparedCompanyIds: List<String>
        get() = companyIds?.toList() ?: emptyList()

    val shouldFilterByReportingPeriod: Boolean
        get() = reportingPeriods?.isNotEmpty() ?: false

    val preparedReportingPeriods: List<String>
        get() = reportingPeriods?.toList() ?: emptyList()

    val shouldFilterByCompanyName: Boolean
        get() = companyName?.isNotEmpty() ?: false

    val shouldFilterByQaStatus: Boolean
        get() = qaStatuses?.isNotEmpty() ?: false

    val preparedQaStatuses: List<String>
        get() = qaStatuses?.map { it.name } ?: emptyList()
}
