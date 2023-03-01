package org.dataland.datalandmessagequeueutils.exceptions

/**
 * A MessageQueueRejectException should be thrown if message received from the RabbitMQ Queue should be rejected
 */
class UnexpectedMessageTypeMessageQueueRejectException(foundType: String, expectedType: String) :
    MessageQueueRejectException("Message has type \"$foundType\" but type \"$expectedType\" was expected")
