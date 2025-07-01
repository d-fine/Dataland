package org.dataland.datalandbackend.repositories.utils

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.utils.CompanyControllerDescriptionsAndExamples
import org.dataland.datalandbackendutils.model.QaStatus
import java.util.UUID

/**
 * A filter class used in the searchDataMetaInformation()-Method which allows
 * convenient usage of SEPL instructions in the query
 */
data class DataMetaInformationSearchFilter(
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String? = null,
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.DATA_TYPE_DESCRIPTION,
    )
    val dataType: DataType? = null,
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String? = null,
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.IS_ONLY_ACTIVE_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.IS_ONLY_ACTIVE_EXAMPLE,
    )
    val onlyActive: Boolean,
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.ALL_UPLOADER_USER_IDS_DESCRIPTION,
        example = CompanyControllerDescriptionsAndExamples.IS_ONLY_ACTIVE_EXAMPLE,
    )
    val uploaderUserIds: Set<UUID>? = null,
    @field:Schema(
        description = CompanyControllerDescriptionsAndExamples.QA_STATUS_DESCRIPTION,
    )
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
    @JsonIgnore
    fun isNullOrEmpty(): Boolean =
        areDataDimensionFiltersEmpty() &&
            uploaderUserIds.isNullOrEmpty() &&
            qaStatus == null &&
            !onlyActive

    @JsonIgnore
    private fun areDataDimensionFiltersEmpty(): Boolean =
        companyId.isNullOrEmpty() &&
            dataType == null &&
            reportingPeriod.isNullOrEmpty()
}
