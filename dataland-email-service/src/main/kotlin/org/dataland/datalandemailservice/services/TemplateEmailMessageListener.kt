package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import org.dataland.datalandbackendutils.utils.getEmailAddress
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
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
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
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val templateEmailFactories: List<TemplateEmailFactory>,
    @Qualifier("AuthenticatedOkHttpClient") val authenticatedOkHttpClient: OkHttpClient,
    @Value("\${dataland.keycloak.base-url}") private val keycloakBaseUrl: String,
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
                value =
                    Queue(
                        "sendTemplateEmailService",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.SEND_EMAIL, declare = "false"),
                key = [RoutingKeyNames.TEMPLATE_EMAIL],
            ),
        ],
    )
    fun sendTemplateEmail(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.SEND_TEMPLATE_EMAIL)
        val message = objectMapper.readValue(jsonString, TemplateEmailMessage::class.java)
        logger.info(
            "Received template email message of type ${message.emailTemplateType.name} " +
                "with correlationId $correlationId.",
        )

        MessageQueueUtils.rejectMessageOnException {
            val receiverEmailAddress = getEmailAddressByRecipient(message.receiver)
            val templateEmailFactory = getMatchingEmailFactory(message)
            emailSender.filterReceiversAndSendEmail(
                templateEmailFactory.buildEmail(
                    receiverEmail = receiverEmailAddress,
                    properties = message.properties,
                ),
            )
        }
    }

    private fun getEmailAddressByRecipient(receiver: TemplateEmailMessage.EmailRecipient): String =
        when (receiver) {
            is TemplateEmailMessage.EmailAddressEmailRecipient ->
                receiver.email
            is TemplateEmailMessage.UserIdEmailRecipient ->
                getEmailAddress(authenticatedOkHttpClient, objectMapper, keycloakBaseUrl, receiver.userId)
        }

    private fun getMatchingEmailFactory(message: TemplateEmailMessage): TemplateEmailFactory =
        templateEmailFactories
            .find { it.builderForType == message.emailTemplateType }
            ?: throw IllegalArgumentException(
                "There is no builder for TemplateEmailMessages" +
                    " with type ${message.emailTemplateType.name}",
            )
}
