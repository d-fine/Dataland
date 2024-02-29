package org.dataland.datalandemailservice.services.templateemail

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.TemplateEmailMessage
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
@Service
class TemplateEmailMessageListener(
    @Autowired private val emailSender: EmailSender,
    @Autowired private val messageQueueUtils: MessageQueueUtils,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val templateEmailBuilders: List<TemplateEmailBuilderBase>,
) {
    private val logger = LoggerFactory.getLogger(TemplateEmailMessageListener::class.java)

    /**
     * Checks if a message object in the queue fits the expected RoutingKey and Internal Type
     * to process it as internal mail
     * @param jsonString the message object which should be sent out as a mail
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "sendTemplateEmailService",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.SendEmail, declare = "false"),
                key = [RoutingKeyNames.internalEmail],
            ),
        ],
    )
    fun sendInternalEmail(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.Type) type: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
    ) {
        messageQueueUtils.validateMessageType(type, MessageType.SendTemplateEmail)
        val templateEmailMessage = objectMapper.readValue(jsonString, TemplateEmailMessage::class.java)
        logger.info("Received template email message of type ${templateEmailMessage.emailTemplateType.name} " +
            "with correlationId $correlationId.")
        messageQueueUtils.rejectMessageOnException {
            val templateEmailBuilder = templateEmailBuilders
                .find { it.builderForType == templateEmailMessage.emailTemplateType}
                ?: throw IllegalArgumentException(
                    "There is no builder for TemplateEmailMessages with typ ${templateEmailMessage.emailTemplateType.name}"
                )
            emailSender.sendEmail(templateEmailBuilder.buildEmail(
                receiverEmail = "",
                properties = templateEmailMessage.properties
            ))
        }
    }
}
