package org.dataland.datalandmessagequeueutils.exceptions

import org.springframework.amqp.AmqpRejectAndDontRequeueException

/**
 * A MessageQueueException should be thrown if message received from the RabbitMQ Queue does not have
 * the expected format
 */
class MessageQueueException() : AmqpRejectAndDontRequeueException("Message could not be processed - Message rejected")
