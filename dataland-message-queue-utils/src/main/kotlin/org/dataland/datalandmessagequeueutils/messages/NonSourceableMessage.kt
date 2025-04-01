package org.dataland.datalandmessagequeueutils.messages

data class NonSourceableMessage(
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
