package org.dataland.datalandbackend.model.companies

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Class that returns all available countryCodes and sectors
 * @param countryCodes List of available countryCodes
 * @param sectors List of available sectors
 */
data class CompanyAvailableDistinctValues(
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.LIST_OF_COUNTRY_CODES_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.LIST_OF_COUNTRY_CODES_EXAMPLE,
            ),
    )
    val countryCodes: Set<String>,
    @field:JsonProperty(required = true)
    @field:ArraySchema(
        arraySchema =
            Schema(
                type = "string",
                description = BackendOpenApiDescriptionsAndExamples.LIST_OF_SECTORS_DESCRIPTION,
                example = BackendOpenApiDescriptionsAndExamples.LIST_OF_SECTORS_EXAMPLE,
            ),
    )
    val sectors: Set<String>,
)
