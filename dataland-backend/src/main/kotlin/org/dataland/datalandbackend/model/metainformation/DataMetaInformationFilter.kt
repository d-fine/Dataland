package org.dataland.datalandbackend.model.metainformation

import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackendutils.model.QaStatus
import java.util.UUID

/**
 * API model specifying fields that can be used when requesting data meta information.
 * @param companyId The ID of a company to look for
 * @param dataType The framework of the data sets
 * @param showOnlyActive A flag to exclude inactive data sets
 * @param reportingPeriod The reporting year of the data sets
 * @param uploaderUserIds A set of user IDs which may hav uploaded the data sets
 * @param qaStatus The QA status of the data sets
 */
data class DataMetaInformationFilter(
    val companyId: String? = null,
    val dataType: DataType? = null,
    val showOnlyActive: Boolean = true,
    val reportingPeriod: String? = null,
    val uploaderUserIds: Set<UUID>? = null,
    val qaStatus: QaStatus? = null,
) {
    fun toDataMetaInformationSearchFilter(): DataMetaInformationSearchFilter =
        DataMetaInformationSearchFilter(
            companyId = companyId,
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            onlyActive = showOnlyActive,
            uploaderUserIds = uploaderUserIds,
            qaStatus = qaStatus,
        )

    fun filterContainsSearchParameter(): Boolean =
        companyId != null || dataType != null || reportingPeriod != null || uploaderUserIds != null || qaStatus != null
}
