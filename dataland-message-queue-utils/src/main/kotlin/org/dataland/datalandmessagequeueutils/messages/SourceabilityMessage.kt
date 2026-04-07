package org.dataland.datalandmessagequeueutils.messages

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import java.time.ZonedDateTime
import java.util.UUID

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

/**
 * Payload for NonSourceabilityCreatedEvent - published by backend when non-sourceability request is created.
 */
data class NonSourceabilityCreatedEventPayload(
    val eventId: UUID,
    val nonSourceabilityId: UUID,
    val companyId: UUID,
    val dataType: String,
    val reportingPeriod: String,
    val reason: String,
    val uploaderUserId: String,
    val uploadTime: ZonedDateTime,
    val eventPublishedTime: ZonedDateTime,
)

/**
 * Payload for NonSourceabilityAutoAcceptedEvent - published by backend when bypassing QA.
 */
data class NonSourceabilityAutoAcceptedEventPayload(
    val eventId: UUID,
    val nonSourceabilityId: UUID,
    val companyId: UUID,
    val dataType: String,
    val reportingPeriod: String,
    val reason: String,
    val uploaderUserId: String,
    val uploadTime: ZonedDateTime,
    val eventPublishedTime: ZonedDateTime,
)
