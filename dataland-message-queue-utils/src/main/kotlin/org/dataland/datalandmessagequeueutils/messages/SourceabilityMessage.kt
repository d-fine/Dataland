package org.dataland.datalandmessagequeueutils.messages

/**
 * Payload of a message concerning the sourceability status of a dataset sent by the backend to the
 * BACKEND_DATA_NON_SOURCEABLE exchange.
 */
data class SourceabilityMessage(
    val companyId: String,
    val dataType: String,
    val reportingPeriod: String,
    val isNonSourceable: Boolean,
    val reason: String,
) {
    /**
     * Returns the boolean information whether the received data is incomplete.
     */
    fun receivedDataIsIncomplete() = companyId.isEmpty() || reportingPeriod.isEmpty()

    /**
     * Returns the boolean information whether the associated dataset was actually set to non-sourceable.
     */
    fun datasetWasSetToNonSourceable() = isNonSourceable
}
