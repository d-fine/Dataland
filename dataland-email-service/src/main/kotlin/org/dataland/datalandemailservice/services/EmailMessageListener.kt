package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandemailservice.email.EmailBuilder
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetGranted
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetRequested
import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApproved
import org.dataland.datalandmessagequeueutils.messages.email.DatasetRequestedClaimOwnership
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.InitializeBaseUrlLater
import org.dataland.datalandmessagequeueutils.messages.email.InitializeSubscriptionUuidLater
import org.dataland.datalandmessagequeueutils.messages.email.MultipleDatasetsUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.SingleDatasetUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailData
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
    fun sendEmail(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.SEND_EMAIL)

        val message = objectMapper.readValue(jsonString, EmailMessage::class.java)
        logger.info(
            "Received email message of type ${message.typedEmailData::class} with correlationId $correlationId.",
        )

        MessageQueueUtils.rejectMessageOnException {
            val receivers = resolveRecipients(message.receiver)
            val cc = resolveRecipients(message.cc)
            val bcc = resolveRecipients(message.bcc)

            val blockedContacts = receivers.blocked + cc.blocked + bcc.blocked
            if (blockedContacts.isNotEmpty()) {
                logger.info("Will not send email to the following blocked contacts: $blockedContacts")
            }

            if (receivers.allowed.isEmpty() && cc.allowed.isEmpty() && bcc.allowed.isEmpty()) {
                logger.info("No email was sent. After filtering the receivers none remained.")
                return@rejectMessageOnException
            }

            val sender = emailContactService.getSenderContact()
            setLateInitVars(message.typedEmailData, receivers.allowed)
            val email = getEmailBuilder(message.typedEmailData)
                .build(sender, receivers.allowed.keys.toList(), cc.allowed.keys.toList(), bcc.allowed.keys.toList())
            emailSender.sendEmail(email)
        }
    }

    private fun resolveRecipients(recipients: List<EmailRecipient>): EmailSubscriptionTracker.FilteredContacts =
        recipients
            .flatMap { emailContactService.getContacts(it) }
            .let { emailSubscriptionTracker.subscribeContactsIfNeededAndFilter(it) }

    private fun setLateInitVars(typedEmailData: TypedEmailData, receivers: Map<EmailContact, UUID>) {
        if (typedEmailData is InitializeBaseUrlLater) {
            typedEmailData.baseUrl = proxyPrimaryUrl
        }
        if (typedEmailData is InitializeSubscriptionUuidLater) {
            require(receivers.size == 1)
            typedEmailData.subscriptionUuid = receivers.values.first().toString()
        }
    }

    private fun getEmailBuilder(typedEmailData: TypedEmailData): EmailBuilder =
        when (typedEmailData) {
            is DatasetRequestedClaimOwnership ->
                EmailBuilder(
                    typedEmailData,
                    "A message from Dataland: Your ESG data are high on demand!",
                    "/html/dataset_requested_claim_ownership.ftl",
                    "/text/dataset_requested_claim_ownership.ftl"
                )
            is CompanyOwnershipClaimApproved ->
                EmailBuilder(
                    typedEmailData,
                    "Your company ownership claim for ${typedEmailData.companyName}" + " is confirmed!",
                    "/html/company_ownership_claim_approved.ftl",
                    "/text/company_ownership_claim_approved.ftl"
                )
            is AccessToDatasetRequested ->
                EmailBuilder(
                    typedEmailData,
                    "Access to your data has been requested on Dataland!",
                    "/html/access_to_dataset_requested.ftl",
                    "/text/access_to_dataset_requested.ftl"
                )
            is SingleDatasetUploadedEngagement ->
                EmailBuilder(
                    typedEmailData,
                    "New data for ${typedEmailData.companyName} on Dataland",
                    "/html/single_dataset_uploaded_engagement.ftl",
                    "/text/single_dataset_uploaded_engagement.ftl"
                )
            is MultipleDatasetsUploadedEngagement ->
                EmailBuilder(
                    typedEmailData,
                    "New data for ${typedEmailData.companyName} on Dataland",
                    "/html/multiple_datasets_uploaded_engagement.ftl",
                    "/text/multiple_datasets_uploaded_engagement.ftl"
                )
            is AccessToDatasetGranted ->
                EmailBuilder(
                    typedEmailData,
                    "Your Dataland Access Request has been granted!",
                    "/html/access_to_dataset_granted.ftl",
                    "/text/access_to_dataset_granted.ftl"
                )
        }


}
