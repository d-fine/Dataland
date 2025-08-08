package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.UserServiceOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * Portfolio API model for POST
 * Request support
 */
data class SupportRequestData(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.HELP_REQUEST_TOPIC,
        example = UserServiceOpenApiDescriptionsAndExamples.HELP_REQUEST_TOPIC_EXAMPLE,
    )
    val topic: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.HELP_REQUEST_MESSAGE,
        example = UserServiceOpenApiDescriptionsAndExamples.HELP_REQUEST_MESSAGE_EXAMPLE,
    )
    val message: String,
)
