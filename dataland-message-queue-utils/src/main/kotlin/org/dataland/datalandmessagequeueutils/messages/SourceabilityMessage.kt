package org.dataland.datalandmessagequeueutils.messages

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Payload of a message concerning the sourceability status of a dataset sent by the backend to the
 * BACKEND_DATA_NONSOURCEABLE exchange.
 */
data class SourceabilityMessage(
    override val companyId: String,
    override val dataType: String,
    override val reportingPeriod: String,
    @JsonProperty(value = "isNonSourceable")
    val isNonSourceable: Boolean,
    val reason: String,
) : MessageWithTriple
