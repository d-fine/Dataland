package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * A filter class used in the searching for unreviewed datasets which allows
 * convenient usage of SEPL instructions in the query
 */
data class QaSearchFilter(
    val dataType: Set<DataTypeEnum>?,
    val reportingPeriod: Set<String>?,
    val companyName: String?,

) {
    val shouldFilterByDataType: Boolean
        get() = dataType?.isNotEmpty() ?: false

    val preparedDataType: List<String>
        get() = dataType?.map { it.value } ?: emptyList()

    val shouldFilterByCompanyName: Boolean
        get() = companyName?.isNotEmpty() ?: false

    val preparedCompanyName: String
        get() = companyName ?: ""

    val shouldFilterByReportingPeriod: Boolean
        get() = reportingPeriod?.isNotEmpty() ?: false

    val preparedReportingPeriod: List<String>
        get() = reportingPeriod?.toList() ?: emptyList()
}
