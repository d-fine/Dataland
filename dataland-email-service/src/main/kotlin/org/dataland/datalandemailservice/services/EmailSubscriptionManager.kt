package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.KeyValueTable
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service responsible for managing email subscriptions.
 */
@Service
class EmailSubscriptionManager(
    @Autowired private val emailSubscriptionRepository: EmailSubscriptionRepository,
    @Autowired private val emailMessageListener: EmailMessageListener,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Unsubscribes an email address from email communications based on the provided UUID and sends a mail
     * to the stakeholders that this email address is now unsubscribed.
     * @param uuid The UUID of the subscription to be unsubscribed.
     * @return A response entity indicating the result of the unsubscription.
     */
    @Transactional
    fun unsubscribeUuidAndInformStakeholders(uuid: UUID): ResponseEntity<String> {
        val emailSubscription = emailSubscriptionRepository.findByUuid(uuid)

        return if (emailSubscription != null) {
            emailSubscription.isSubscribed = false
            informStakeholdersOfUnsubscription(emailSubscription.emailAddress)
            val successMessage = "Successfully unsubscribed email address corresponding to UUID: $uuid."
            logger.info(successMessage)
            ResponseEntity.ok(successMessage)
        } else {
            val errorMessage = "There is no email address corresponding to UUID: $uuid."
            logger.info(errorMessage)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage)
        }
    }

    /**
     * Send a mail with the information who unsubscribed to the stakeholders.
     *
     * @param unsubscribedEmailAddress The email address of the person who unsubscribed.
     * @return This method has no return value.
     *
     */
    private fun informStakeholdersOfUnsubscription(unsubscribedEmailAddress: String) {
        val emailMessage = EmailMessage(
            KeyValueTable(
                subject = "A user has unsubscribed from data uploads notifications",
                textTitle = "A user has unsubscribed from data uploads notifications.",
                htmlTitle = "A user has unsubscribed from data uploads notifications.",
                table = listOf(
                    "Unsubscribed Email Address" to Value.Text(unsubscribedEmailAddress)
                )
            ),
            listOf(EmailRecipient.Internal),
            listOf(EmailRecipient.InternalCc),
            emptyList()
        )
        emailMessageListener.buildAndSendEmail(emailMessage)
    }
}
