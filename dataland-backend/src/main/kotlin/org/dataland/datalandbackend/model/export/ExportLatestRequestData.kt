package org.dataland.datalandbackend.model.export

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.interfaces.export.ExportLatestRequestData
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * API model class representing the data required to request an export of latest available data.
 */
data class ExportLatestRequestData(
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIODS_LIST_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.REPORTING_PERIODS_LIST_EXAMPLE,
            ),
    )
    override val companyIds: List<String>,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.FILE_FORMAT_DESCRIPTION,
    )
    override val fileFormat: ExportFileType,
) : ExportLatestRequestData
