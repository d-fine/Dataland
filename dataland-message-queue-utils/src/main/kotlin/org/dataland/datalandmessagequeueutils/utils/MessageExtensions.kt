package org.dataland.datalandmessagequeueutils.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.springframework.amqp.core.Message

/**
 * Read message payload from message body using object mapper
 */
inline fun <reified T> Message.readMessagePayload(objectMapper: ObjectMapper): T {
    val content = String(this.body)
    return MessageQueueUtils.readMessagePayload<T>(content, objectMapper)
}

/**
 * Get message correlation id from headers
 */
fun Message.getCorrelationId(): String = this.messageProperties.headers[MessageHeaderKey.CORRELATION_ID].toString()

/**
 * Get message type from headers
 */
fun Message.getType(): String = this.messageProperties.headers[MessageHeaderKey.TYPE].toString()
