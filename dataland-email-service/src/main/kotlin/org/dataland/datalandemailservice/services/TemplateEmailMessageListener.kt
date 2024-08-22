package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandemailservice.services.templateemail.TemplateEmailFactory
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
    @Autowired private val templateEmailFactories: List<TemplateEmailFactory>,
    @Autowired private val keycloakUserControllerApiService: KeycloakUserControllerApiService,
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
                key = [RoutingKeyNames.templateEmail],
            ),
        ],
    )
    fun sendTemplateEmail(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.Type) type: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
    ) {
        messageQueueUtils.validateMessageType(type, MessageType.SendTemplateEmail)
        val message = objectMapper.readValue(jsonString, TemplateEmailMessage::class.java)
        logger.info(
            "Received template email message of type ${message.emailTemplateType.name} " +
                "with correlationId $correlationId.",
        )

        val receiverEmailAddress = getEmailAddressByRecipient(objectMapper.readTree(jsonString).get("receiver"))
        messageQueueUtils.rejectMessageOnException {
            val templateEmailFactory = getMatchingEmailFactory(message)
            emailSender.sendEmailWithoutTestReceivers(
                templateEmailFactory.buildEmail(
                    receiverEmail = receiverEmailAddress,
                    properties = message.properties,
                ),
            )
        }
    }

    private fun getEmailAddressByRecipient(receiver: JsonNode?): String {
        return when (val receiverType = receiver?.get("type")?.asText()) {
            "address" -> receiver.get("email").asText()
            "user" -> keycloakUserControllerApiService.getEmailAddress(receiver.get("userId").asText())
            else -> {
                throw IllegalArgumentException("Invalid receiver type: $receiverType")
            }
        }
    }

    private fun getMatchingEmailFactory(message: TemplateEmailMessage): TemplateEmailFactory {
        return templateEmailFactories
            .find { it.builderForType == message.emailTemplateType }
            ?: throw IllegalArgumentException(
                "There is no builder for TemplateEmailMessages" +
                    " with type ${message.emailTemplateType.name}",
            )
    }
}
