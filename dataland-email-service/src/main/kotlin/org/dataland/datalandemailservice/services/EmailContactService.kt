package org.dataland.datalandemailservice.services

import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.utils.EmailStringConverter
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.system.exitProcess

/**
 * Service used to convert EmailRecipient objects to EmailContact objects and also to retrieve the sender contact.
 * */
@Service
class EmailContactService(
    @Autowired private val keycloakUserService: KeycloakUserService,
    @Value("\${dataland.notification.internal.receivers}") private val semicolonSeparatedInternalRecipients: String,
    @Value("\${dataland.notification.internal.cc}") private val semicolonSeparatedInternalCcRecipients: String,
    @Value("\${dataland.notification.sender.address}") private val senderEmail: String,
    @Value("\${dataland.notification.sender.name}") private val senderName: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val internalContacts: List<EmailContact>
    private val internalCcContacts: List<EmailContact>

    init {
        try {
            internalContacts = EmailStringConverter.convertEmailsJoinedStringToListOfEmailContacts(semicolonSeparatedInternalRecipients)
            internalCcContacts = EmailStringConverter.convertEmailsJoinedStringToListOfEmailContacts(semicolonSeparatedInternalCcRecipients)
        } catch (exception: IllegalArgumentException) {
            logger.error(
                exception.message,
            )
            exitProcess(1)
        }
    }

    /**
     * Retrieves a list of email contacts based on the specified recipient type.
     *
     * @param recipient The `EmailRecipient` specifying the type and details of the recipient.
     * @return A list of `EmailContact` objects corresponding to the recipient.
     * @throws IllegalArgumentException If the recipient is of type `EmailRecipient.UserId` and the user's email is `null`.
     */
    fun getContacts(recipient: EmailRecipient): List<EmailContact> =
        when (recipient) {
            is EmailRecipient.EmailAddress ->
                listOf(EmailContact.create(recipient.email))
            is EmailRecipient.UserId -> {
                val emailAddress =
                    keycloakUserService.getUser(recipient.userId).email
                        ?: throw IllegalArgumentException("User with ${recipient.userId} not found or has no email address")
                listOf(EmailContact.create(emailAddress, null))
            }
            is EmailRecipient.Internal ->
                internalContacts
            is EmailRecipient.InternalCc ->
                internalCcContacts
        }

    /**
     * Retrieves the email contact information use to send emails.
     *
     * @return An `EmailContact` representing the sender, containing the sender's email and name.
     */
    fun getSenderContact(): EmailContact = EmailContact.create(senderEmail, name = senderName)
}
