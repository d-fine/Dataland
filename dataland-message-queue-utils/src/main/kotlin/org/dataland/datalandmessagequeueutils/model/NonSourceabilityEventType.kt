package org.dataland.datalandmessagequeueutils.model

/**
 * Event types for non-sourceability lifecycle propagation.
 */
enum class NonSourceabilityEventType {
    CREATED,
    AUTO_ACCEPTED,
    QA_ACCEPTED,
    QA_REJECTED,
}
