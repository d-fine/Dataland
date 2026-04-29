package org.dataland.datalandbackend.model.metainformation

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.BackendOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples

/**
 * API request model for submitting a non-sourceability request.
 */
data class NonSourceabilityRequest(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val companyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(description = GeneralOpenApiDescriptionsAndExamples.DATA_TYPE_DESCRIPTION)
    val dataType: DataType,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.REPORTING_PERIOD_EXAMPLE,
    )
    val reportingPeriod: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = BackendOpenApiDescriptionsAndExamples.REASON_DESCRIPTION,
        example = BackendOpenApiDescriptionsAndExamples.REASON_EXAMPLE,
    )
    val reason: String,
    @field:JsonProperty(required = false)
    @field:Schema(
        description =
            "When true the non-sourceability entry is immediately activated without QA review. " +
                "Requires ROLE_ADMIN.",
    )
    val bypassQa: Boolean = false,
    @field:JsonProperty(required = true)
    @field:Schema(
        description =
            "When true the triple is treated as non-sourceable; when false the entry is inactive. " +
                "Must be false when bypassQa=false (the QA service sets this to true upon approval). " +
                "When bypassQa=true, signals whether the intent is to mark the triple as non-sourceable (true) " +
                "or sourceable again (false).",
    )
    val currentlyActive: Boolean,
)
