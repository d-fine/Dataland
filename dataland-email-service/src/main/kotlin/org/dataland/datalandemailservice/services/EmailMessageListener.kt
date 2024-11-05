package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandemailservice.email.EmailBuilder
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.DatasetRequestedClaimOwnership
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailData
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
 * TODO
 */
@Service
class EmailMessageListener(
    @Autowired private val emailSender: EmailSender,
    @Autowired private val messageQueueUtils: MessageQueueUtils,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val emailContactService: EmailContactService,
    @Autowired private val emailSubscriptionTracker: EmailSubscriptionTracker
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
        messageQueueUtils.validateMessageType(type, MessageType.SEND_TEMPLATE_EMAIL)

        val message = objectMapper.readValue(jsonString, EmailMessage::class.java)
        logger.info(
            "Received template email message of type ${message.typedEmailData::class} " +
                    "with correlationId $correlationId.",
        )

        messageQueueUtils.rejectMessageOnException {

            val receivers = resolveRecipients(message.receiver)
            val cc = resolveRecipients(message.cc)
            val bcc = resolveRecipients(message.bcc)

            if (receivers.isEmpty() && cc.isEmpty() && bcc.isEmpty()) {
                // TODO log there are no receivers and we bail
                return@rejectMessageOnException
            }

            val sender = emailContactService.getSenderContact()

            // TODO do lateInit of vars, should be another method

            val email = getEmailBuilder(message.typedEmailData).build(sender, receivers, cc, bcc)

            // TODO do not filter there anymore
            emailSender.filterReceiversAndSendEmail(email)
        }
    }

    private fun resolveRecipients(recipients: List<EmailRecipient>): List<EmailContact> =
        recipients
            .flatMap { emailContactService.getContacts(it) }
            .filter { emailSubscriptionTracker.shouldSendToEmailContact(it) }


    private fun getEmailBuilder(typedEmailData: TypedEmailData): EmailBuilder {
        return when (typedEmailData) {
            is DatasetRequestedClaimOwnership ->
                EmailBuilder(
                    typedEmailData,
                    "A message from Dataland: Your ESG data are high on demand!",
                    "/claim_ownership.html.ftl",
                    "TODO"
                )
        }
    }

}
