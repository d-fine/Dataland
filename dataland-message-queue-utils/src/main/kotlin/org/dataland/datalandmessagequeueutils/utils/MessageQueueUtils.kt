package org.dataland.datalandmessagequeueutils.utils

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.json.JSONObject
import java.util.UUID

/**
 * MessageQueueUtils provides utility functions to be used with the processing of messages
 */
object MessageQueueUtils {
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
    fun validateMessageType(
        messageType: String,
        expectedType: String,
    ) {
        if (messageType != expectedType) {
            throw MessageQueueRejectException(
                "Message has type \"$messageType\" but type \"$expectedType\" was expected",
            )
        }
    }

    /**
     * Reads the json payload message received from the queue into the generic T type.
     * If a jackson exception is thrown, it is caught and rethrown as MessageQueueRejectException.
     */
    inline fun <reified T> readMessagePayload(
        jsonString: String,
        objectMapper: ObjectMapper,
    ): T {
        try {
            return objectMapper.readValue(jsonString, T::class.java)
        } catch (e: JacksonException) {
            throw MessageQueueRejectException("Failed to parse json into ${T::class.qualifiedName}, json: $jsonString, exception: $e\"")
        }
    }

    /**
     * Extracts the data ID from a message payload. Throws
     * @throws MessageQueueRejectException if no data ID is found
     */
    fun getDataId(payload: String): String {
        val dataId = JSONObject(payload).getString("dataId")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        return dataId
    }

    /**
     * Validates a data ID by checking if it is a valid UUID
     * @throws IllegalArgumentException if the data ID is not a valid UUID
     */
    fun validateDataId(dataId: String) {
        UUID.fromString(dataId)
    }
}
