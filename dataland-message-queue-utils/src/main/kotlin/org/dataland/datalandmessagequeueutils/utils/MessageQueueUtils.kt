package org.dataland.datalandmessagequeueutils.utils

import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.springframework.stereotype.Component

/**
 * MessageQueueUtils provides utility functions to be used with the processing of messages
 */
@Component("MessageQueueUtils")
class MessageQueueUtils {
    /**
     * rejectMessageOnException is a wrapper function which provides a possibility to add a try catch block to a
     * function
     * The catch will throw a MessageQueueRejectException
     * @param method is the method to be wrapped
     */
    @Suppress("TooGenericExceptionCaught")
    fun <R> rejectMessageOnException(method: () -> R): R {
        try {
            return method()
        } catch (e: Exception) {
            throw MessageQueueRejectException(e.message, e)
        }
    }

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
