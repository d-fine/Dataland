package org.dataland.datalandbackend.model.export

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.interfaces.export.ExportRequestData
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
    override val reportingPeriods: List<String>,
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.COMPANY_IDS_LIST_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.COMPANY_IDS_LIST_EXAMPLE,
            ),
    )
    override val companyIds: List<String>,
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.FILE_FORMAT_DESCRIPTION,
    )
    override val fileFormat: ExportFileType,
    @field:Schema(
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        description =
            "Maps the ID of a proxy company to the IDs of the companies it proxies. " +
                "Used to fill the 'PROXY_FOR' column of the export.",
    )
    override val proxiedCompanyIdsByProxyCompanyId: Map<String, List<String>> = emptyMap(),
) : ExportRequestData
