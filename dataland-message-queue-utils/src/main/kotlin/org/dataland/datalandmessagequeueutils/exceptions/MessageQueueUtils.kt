package org.dataland.datalandmessagequeueutils.exceptions

/**
 * MessageQueueUtils provides utility functions for working with a message queue
 */
object MessageQueueUtils {
    /**
     * Handles a message type comparison and rejects the message in case of a missmatch
     */
    fun validateMessageType(messageType: String, expectedType: String) {
        if (messageType != expectedType) {
            throw MessageQueueRejectException(
                "Message has type \"$messageType\" but type \"$expectedType\" was expected",
            )
        }
    }
}
