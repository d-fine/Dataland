package org.dataland.datalandbackend.model.metainformation

import org.dataland.datalandbackend.model.DataType
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
data class DataMetaInformationRequest(
    val companyId: String?,
    val dataType: DataType?,
    val showOnlyActive: Boolean = true,
    val reportingPeriod: String?,
    val uploaderUserIds: Set<UUID>?,
    val qaStatus: QaStatus?,
)
