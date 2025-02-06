package org.dataland.datalandbackend.repositories.utils

import com.fasterxml.jackson.annotation.JsonIgnore
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
    @get:JsonIgnore
    val shouldFilterByCompanyId: Boolean
        get() = !companyId.isNullOrEmpty()

    @get:JsonIgnore
    val preparedCompanyId: String
        get() = companyId ?: ""

    @get:JsonIgnore
    val shouldFilterByDataType: Boolean
        get() = dataType != null

    @get:JsonIgnore
    val preparedDataType: String
        get() = dataType?.name ?: ""

    @get:JsonIgnore
    val shouldFilterByReportingPeriod: Boolean
        get() = !reportingPeriod.isNullOrEmpty()

    @get:JsonIgnore
    val preparedReportingPeriod: String
        get() = reportingPeriod ?: ""

    @get:JsonIgnore
    val shouldFilterByUploaderUserIds: Boolean
        get() = !uploaderUserIds.isNullOrEmpty()

    @get:JsonIgnore
    val preparedUploaderUserIds: List<String>
        get() = uploaderUserIds?.map { it.toString() } ?: listOf()

    @get:JsonIgnore
    val shouldFilterByQaStatus: Boolean
        get() = qaStatus != null

    @get:JsonIgnore
    val preparedQaStatus: QaStatus
        get() = qaStatus ?: QaStatus.Accepted

    /**
     * Checks if the filter contains any search parameters
     */
    fun isNullOrEmpty(): Boolean =
        companyId.isNullOrEmpty() &&
            dataType == null &&
            reportingPeriod.isNullOrEmpty() &&
            uploaderUserIds.isNullOrEmpty() &&
            qaStatus == null &&
            !onlyActive
}
