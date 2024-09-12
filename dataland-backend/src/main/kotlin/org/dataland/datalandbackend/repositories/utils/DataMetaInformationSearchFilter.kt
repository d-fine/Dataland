package org.dataland.datalandbackend.repositories.utils

import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.model.QaStatus
import java.util.*

/**
 * A filter class used in the searchDataMetaInformation()-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class DataMetaInformationSearchFilter(
    val companyId: String?,
    val dataType: DataType?,
    val reportingPeriod: String?,
    val onlyActive: Boolean,
    val uploaderUserIds: Set<UUID>?,
    val qaStatus: QaStatus?,
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

    val shouldFilterByUploaderUserIds: Boolean
        get() = !uploaderUserIds.isNullOrEmpty()
}
