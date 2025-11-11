package org.dataland.datalandmessagequeueutils.messages

/**
 * An interface for messages that contain a companyId, dataType, and reportingPeriod
 */
interface MessageWithTriple {
    val companyId: String
    val dataType: String
    val reportingPeriod: String
}
