package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

/**
 * A class that acts like a consumer (receiver) on the sendInternalEmailService queue and forward the message to the
 * mailjet client
 */
@Service("InternalEmailMessageListener")
class InternalEmailMessageListener(
    @Autowired private val internalEmailBuilder: InternalEmailBuilder,
    @Autowired private val emailSender: EmailSender,
    @Autowired private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(InternalEmailMessageListener::class.java)

    /**
     * Checks if a message object in the queue fits the expected RoutingKey and Internal Type
     * to process it as internal mail
     * @param jsonString the message object which should be sent out as a mail
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "sendInternalEmailService",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.SEND_EMAIL, declare = "false"),
                key = [RoutingKeyNames.INTERNAL_EMAIL],
            ),
        ],
    )
    fun sendInternalEmail(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.SEND_INTERNAL_EMAIL)
        val internalEmailMessage = objectMapper.readValue(jsonString, InternalEmailMessage::class.java)
        logger.info("Received internal email message with correlationId $correlationId.")

        MessageQueueUtils.rejectMessageOnException {
            emailSender.filterReceiversAndSendEmail(internalEmailBuilder.buildInternalEmail(internalEmailMessage))
        }
    }
}
