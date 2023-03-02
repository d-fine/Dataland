package org.dataland.datalandmessagequeueutils.exceptions

import org.springframework.amqp.AmqpRejectAndDontRequeueException

/**
 * A MessageQueueRejectException should be thrown to reject a message received from the RabbitMQ Queue
 */
open class MessageQueueRejectException : AmqpRejectAndDontRequeueException {
    constructor (message: String) : super("Message was rejected: $message")
    constructor (cause: Throwable) : super(cause)
}
