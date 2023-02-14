package org.dataland.datalandmessagequeueutils.exceptions

/**
 * A MessageQueueException should be thrown if message received from the RabbitMQ Queue does not have
 * the expected format
 */
class MessageQueueException(message: String) : Exception(message)
