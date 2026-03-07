package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.validateIsEmailAddress
import org.dataland.datalandcommunitymanager.model.inquiry.InquiryData
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.InternalEmailContentTable
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

private const val MAX_CONTACT_NAME_LENGTH = 200
private const val MAX_ORGANISATION_LENGTH = 200
private const val MAX_CONTACT_EMAIL_LENGTH = 320
private const val MAX_MESSAGE_LENGTH = 5000

/**
 * Service for validating and dispatching email notifications for visitor inquiries submitted via the contact form.
 */
@Service
class InquiryNotificationService
    @Autowired
    constructor(
        private val cloudEventMessageHandler: CloudEventMessageHandler,
        private val objectMapper: ObjectMapper,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Validates the inquiry payload and dispatches an email notification to the Dataland team.
         * Returns normally on success. Throws [InvalidInputApiException] (→ 400) for invalid input,
         * or [InternalServerErrorApiException] (→ 500) if email dispatch fails.
         */
        fun processInquiry(inquiryData: InquiryData) {
            val correlationId = UUID.randomUUID().toString()
            validateInquiry(inquiryData)
            try {
                sendEmailNotification(inquiryData, correlationId)
            } catch (e: AmqpException) {
                logger.warn("Inquiry notification dispatch failed. Exception: ${e.message}")
                throw InternalServerErrorApiException(
                    publicSummary = "Notification dispatch failed",
                    publicMessage = "The inquiry could not be forwarded. Please try again later.",
                    internalMessage = "Failed to dispatch inquiry notification email for correlationId=$correlationId",
                    internalCause = e,
                )
            }
            // PII note: contactEmail and message are intentionally excluded from logs
            // contactName has passed CRLF validation above so it is safe to include in logs
            logger.info("inquiry_received contactName={}", inquiryData.contactName)
        }

        private fun validateInquiry(inquiryData: InquiryData) {
            if (inquiryData.contactName.isBlank()) {
                throw InvalidInputApiException("Blank contact name", "The contact name must not be blank.")
            }
            if (inquiryData.contactName.contains('\n') || inquiryData.contactName.contains('\r')) {
                throw InvalidInputApiException(
                    "Invalid contact name",
                    "The contact name must not contain newline characters.",
                )
            }
            if (inquiryData.contactName.length > MAX_CONTACT_NAME_LENGTH) {
                throw InvalidInputApiException(
                    "Contact name too long",
                    "The contact name must not exceed $MAX_CONTACT_NAME_LENGTH characters.",
                )
            }
            inquiryData.organisation?.let { org ->
                if (org.isBlank()) {
                    throw InvalidInputApiException(
                        "Blank organisation",
                        "The organisation must not be blank if provided.",
                    )
                }
                if (org.length > MAX_ORGANISATION_LENGTH) {
                    throw InvalidInputApiException(
                        "Organisation too long",
                        "The organisation must not exceed $MAX_ORGANISATION_LENGTH characters.",
                    )
                }
            }
            if (inquiryData.contactEmail.length > MAX_CONTACT_EMAIL_LENGTH) {
                throw InvalidInputApiException(
                    "Contact email too long",
                    "The contact email must not exceed $MAX_CONTACT_EMAIL_LENGTH characters.",
                )
            }
            inquiryData.contactEmail.validateIsEmailAddress()
            if (inquiryData.message.isBlank()) {
                throw InvalidInputApiException("Blank message", "The message must not be blank.")
            }
            if (inquiryData.message.length > MAX_MESSAGE_LENGTH) {
                throw InvalidInputApiException(
                    "Message too long",
                    "The message must not exceed $MAX_MESSAGE_LENGTH characters.",
                )
            }
        }

        private fun sendEmailNotification(
            inquiryData: InquiryData,
            correlationId: String,
        ) {
            val emailContent =
                InternalEmailContentTable(
                    subject = "[Dataland Inquiry] Contact from ${inquiryData.contactName}",
                    title = "A new inquiry has been submitted via the Dataland contact form.",
                    table =
                        listOf(
                            "Name" to Value.Text(inquiryData.contactName),
                            "Organisation" to Value.Text(inquiryData.organisation ?: "-"),
                            "Email" to Value.Text(inquiryData.contactEmail),
                            "Message" to Value.Text(inquiryData.message),
                        ),
                )
            val message =
                EmailMessage(
                    emailContent,
                    listOf(EmailRecipient.Internal),
                    listOf(EmailRecipient.InternalCc),
                    emptyList(),
                )
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(message),
                MessageType.SEND_EMAIL,
                correlationId,
                ExchangeName.SEND_EMAIL,
                RoutingKeyNames.EMAIL,
            )
        }
    }