package org.dataland.datalandbackend.interfaces

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.BackendOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Interface containing only a company's id and name
 */
interface CompanyIdAndName {
    @get:JsonProperty(required = true)
    @get:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String

    @get:JsonProperty(required = true)
    @get:Schema(
        description = BackendOpenApiDescriptionsAndExamples.COMPANY_NAME_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.COMPANY_NAME_EXAMPLE,
    )
    val companyName: String
}
