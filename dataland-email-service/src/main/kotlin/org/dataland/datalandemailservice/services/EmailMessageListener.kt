package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandemailservice.email.build
import org.dataland.datalandemailservice.email.setLateInitVars
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.util.*

/**
 * TODO
 */
@Service
class EmailMessageListener(
    @Autowired private val emailSender: EmailSender,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val emailContactService: EmailContactService,
    @Autowired private val emailSubscriptionTracker: EmailSubscriptionTracker,
    @Value("\${dataland.proxy.primary.url}") private val proxyPrimaryUrl: String,
) {
    private val logger = LoggerFactory.getLogger(EmailMessageListener::class.java)

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
                    "sendEmailService",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.SEND_EMAIL, declare = "false"),
                key = [RoutingKeyNames.EMAIL],
            ),
        ],
    )
    fun handleSendEmailMessage(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.SEND_EMAIL)

        val message = objectMapper.readValue(jsonString, EmailMessage::class.java)
        logger.info(
            "Received email message of type ${message.typedEmailContent::class} with correlationId $correlationId.",
        )

        MessageQueueUtils.rejectMessageOnException {
            buildAndSendEmail(message)
        }
    }

    fun buildAndSendEmail(emailMessage: EmailMessage) {
        val receivers = resolveRecipients(emailMessage.receiver)
        val cc = resolveRecipients(emailMessage.cc)
        val bcc = resolveRecipients(emailMessage.bcc)

        val blockedContacts = receivers.blocked + cc.blocked + bcc.blocked
        if (blockedContacts.isNotEmpty()) {
            logger.info("Will not send email to the following blocked contacts: $blockedContacts")
        }

        if (receivers.allowed.isEmpty() && cc.allowed.isEmpty() && bcc.allowed.isEmpty()) {
            logger.info("No email was sent. After filtering the receivers none remained.")
            return
        }

        val sender = emailContactService.getSenderContact()
        emailMessage.typedEmailContent.setLateInitVars(receivers.allowed, proxyPrimaryUrl)
        val content = emailMessage.typedEmailContent.build()
        val email = Email(sender, receivers.allowed.keys.toList(), cc.allowed.keys.toList(), bcc.allowed.keys.toList(), content)
        emailSender.sendEmail(email)
    }

    private fun resolveRecipients(recipients: List<EmailRecipient>): EmailSubscriptionTracker.FilteredContacts =
        recipients
            .flatMap { emailContactService.getContacts(it) }
            .let { emailSubscriptionTracker.subscribeContactsIfNeededAndFilter(it) }

}
