package org.dataland.datalandmessagequeueutils.messages

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackendutils.model.BasicDataDimensions

/**
 * Payload of a message concerning the sourceability status of a dataset sent by the backend to the
 * BACKEND_DATA_NONSOURCEABLE exchange.
 */
data class SourceabilityMessage(
    val basicDataDimensions: BasicDataDimensions,
    @get:JsonProperty(value = "isNonSourceable")
    @field:JsonProperty(value = "isNonSourceable")
    val isNonSourceable: Boolean,
    val reason: String,
)
