package org.dataland.datalandmessagequeueutils.model

/**
 * Discriminator for the four non-sourceability lifecycle events exchanged between
 * dataland-backend, dataland-qa-service, and dataland-data-sourcing-service.
 */
enum class NonSourceabilityEventType {
    NON_SOURCEABILITY_CREATED,
    NON_SOURCEABILITY_AUTO_ACCEPTED,
    NON_SOURCEABILITY_QA_ACCEPTED,
    NON_SOURCEABILITY_QA_REJECTED,
}
