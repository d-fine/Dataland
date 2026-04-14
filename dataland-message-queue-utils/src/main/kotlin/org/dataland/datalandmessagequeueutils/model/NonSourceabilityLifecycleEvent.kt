package org.dataland.datalandmessagequeueutils.model

/**
 * Payload carried on all four non-sourceability lifecycle events.
 * The [nonSourceabilityId] is the correlation key that links records across services.
 */
data class NonSourceabilityLifecycleEvent(
    val nonSourceabilityId: String,
    val companyId: String,
    val dataType: String,
    val reportingPeriod: String,
    val eventType: NonSourceabilityEventType,
)
