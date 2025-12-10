package org.dataland.datalandbackend.model.export

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * API model class representing the data required to request an export.
 */
data class ExportRequestData(
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIODS_LIST_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIODS_LIST_EXAMPLE,
            ),
    )
    val reportingPeriods: List<String>,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.COMPANY_IDS_LIST_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.COMPANY_IDS_LIST_EXAMPLE,
            ),
    )
    val companyIds: List<String>,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.FILE_FORMAT_DESCRIPTION,
    )
    val fileFormat: ExportFileType,
)
