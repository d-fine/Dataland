package org.dataland.datasourcingservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains all necessary info that a user has to provide in order to request a bulk of datasets on Dataland.
 * @param companyIdentifiers contains company identifiers for which the user wants to request framework data
 * @param dataTypes contains the names of frameworks, for which the user wants to request framework data
 */
data class BulkDataRequest(
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = DataSourcingOpenApiDescriptionsAndExamples.BULK_REQUEST_COMPANY_IDENTIFIERS_DESCRIPTION,
                example = DataSourcingOpenApiDescriptionsAndExamples.BULK_REQUEST_COMPANY_IDENTIFIERS_EXAMPLE,
            ),
    )
    val companyIdentifiers: Set<String>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                implementation = DataTypeEnum::class,
                description = DataSourcingOpenApiDescriptionsAndExamples.BULK_REQUEST_DATA_TYPES_DESCRIPTION,
                example = DataSourcingOpenApiDescriptionsAndExamples.BULK_REQUEST_DATA_TYPES_EXAMPLE,
            ),
    )
    val dataTypes: Set<DataTypeEnum>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = DataSourcingOpenApiDescriptionsAndExamples.BULK_REQUEST_REPORTING_PERIODS_DESCRIPTION,
                example = DataSourcingOpenApiDescriptionsAndExamples.BULK_REQUEST_REPORTING_PERIODS_EXAMPLE,
            ),
    )
    val reportingPeriods: Set<String>,
)
