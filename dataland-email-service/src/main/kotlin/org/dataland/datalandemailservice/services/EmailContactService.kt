package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.utils.isEmailAddress
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * TODO
 * */
@Service
class EmailContactService(
    @Autowired private val objectMapper: ObjectMapper,
    @Qualifier("AuthenticatedOkHttpClient") val authenticatedOkHttpClient: OkHttpClient,
    @Value("\${dataland.keycloak.base-url}") private val keycloakBaseUrl: String,
    @Value("\${dataland.notification.internal.receivers}") private val semicolonSeparatedInternalRecipients: String,
    @Value("\${dataland.notification.internal.cc}") private val semicolonSeparatedInternalCcRecipients: String,
    @Value("\${dataland.notification.sender.address}") private val senderEmail: String,
    @Value("\${dataland.notification.sender.name}") private val senderName: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val internalContacts: List<EmailContact> = getEmailContactsFromString(semicolonSeparatedInternalRecipients)
    private val internalCcContacts: List<EmailContact> = getEmailContactsFromString(semicolonSeparatedInternalCcRecipients)

    private fun getEmailContactsFromString(semicolonSeperatedEmailAddresses: String): List<EmailContact> =
        semicolonSeperatedEmailAddresses.split(";").mapNotNull { emailAddress ->
            if (emailAddress.isEmailAddress()) {
                EmailContact(emailAddress)
            } else {
                logger.error(
                    "One email address provided by the Spring properties has a wrong format. " +
                    "The following email address was parsed from that prop and caused this error: $emailAddress" +
                    "This email address is ignored.")
                // TODO should the spring service shutdown??
                null
            }
        }

    /**
     * TODO
     */
    fun getContacts(recipient: EmailRecipient): List<EmailContact> {
        return when (recipient) {
            is EmailRecipient.EmailAddress ->
                listOf(EmailContact(recipient.email))
            is EmailRecipient.UserId -> {
                val keycloakUser = getUser(recipient.userId) ?: return emptyList()
                val emailAddress = keycloakUser.email ?: return emptyList()
                listOf(EmailContact(emailAddress, keycloakUser.firstName, keycloakUser.lastName))
            }
            is EmailRecipient.Internal ->
                internalContacts
            is EmailRecipient.InternalCc ->
                internalCcContacts
        }
    }

    fun getSenderContact(): EmailContact = EmailContact(senderEmail, lastName = senderName)

    private fun getUser(userId: String): KeycloakUserInfo? {
        // TODO this is just copy paste add should be somewhere else
        // or retrieve old logic if sufficient
        val request =
            Request
                .Builder()
                .url("$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userId")
                .build()
        val response =
            authenticatedOkHttpClient
                .newCall(request)
                .execute()
                .body!!
                .string()

        try {
            val user =
                objectMapper.readValue(
                    response,
                    KeycloakUserInfo::class.java,
                )
            return user
        } catch (e: JacksonException) {
            logger.warn("Failed to parse response from Keycloak. userId $userId. Response $response, exception: $e")
            return null
        }
    }
}