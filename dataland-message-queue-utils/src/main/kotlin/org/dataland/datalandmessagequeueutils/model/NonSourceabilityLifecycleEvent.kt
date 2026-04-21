package org.dataland.datalandmessagequeueutils.model

/**
 * Payload carried on non-sourceability lifecycle events.
 * The [nonSourceabilityId] is the correlation key that links records across services.
 * The event variant (created / auto-accepted / QA-accepted / QA-rejected) is expressed
 * exclusively via the RabbitMQ MessageType header — no redundant [eventType] field needed.
 */
data class NonSourceabilityLifecycleEvent(
    val nonSourceabilityId: String,
    val companyId: String,
    val dataType: String,
    val reportingPeriod: String,
)
