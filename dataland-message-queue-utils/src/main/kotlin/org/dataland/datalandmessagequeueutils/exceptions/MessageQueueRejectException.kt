package org.dataland.datalandmessagequeueutils.exceptions

import org.springframework.amqp.AmqpRejectAndDontRequeueException

/**
 * A MessageQueueRejectException should be thrown if message received from the RabbitMQ Queue should be rejected
 */
open class MessageQueueRejectException(message: String) :
    AmqpRejectAndDontRequeueException("Message was rejected: $message")
