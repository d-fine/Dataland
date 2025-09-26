package org.dataland.datasourcingservice.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.DataSourcingOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Contains all information a user receives regarding a single data request he performed on Dataland.
 * @param id the id of the newly created data request
 */
data class SingleRequestResponse(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = DataSourcingOpenApiDescriptionsAndExamples.STORED_REQUEST_ID_DESCRIPTION,
        example = DataSourcingOpenApiDescriptionsAndExamples.STORED_REQUEST_ID_EXAMPLE,
    )
    val id: String,
)
