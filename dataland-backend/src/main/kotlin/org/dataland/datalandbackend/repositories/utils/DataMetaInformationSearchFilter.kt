package org.dataland.datalandbackend.repositories.utils

import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus
import java.util.UUID

/**
 * A filter class used in the searchDataMetaInformation()-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class DataMetaInformationSearchFilter(
    val companyId: String? = null,
    val dataType: DataType? = null,
    val reportingPeriod: String? = null,
    val onlyActive: Boolean,
    val uploaderUserIds: Set<UUID>? = null,
    val qaStatus: QaStatus? = null,
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

    val shouldFilterByUploaderUserIds: Boolean
        get() = !uploaderUserIds.isNullOrEmpty()

    val preparedUploaderUserIds: List<String>
        get() = uploaderUserIds?.map { it.toString() } ?: listOf()

    val shouldFilterByQaStatus: Boolean
        get() = qaStatus != null

    val preparedQaStatus: QaStatus
        get() = qaStatus ?: QaStatus.Accepted
}
