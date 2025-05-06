package org.dataland.datalandmessagequeueutils.messages.data

/**
 * Interface for all payloads sent via message queue which correspond to data events
 */
interface DataPayload {
    val dataId: String
}
