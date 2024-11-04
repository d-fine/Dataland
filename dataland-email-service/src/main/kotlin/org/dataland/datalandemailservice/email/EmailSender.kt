package org.dataland.datalandemailservice.email

import com.mailjet.client.MailjetClient
import com.mailjet.client.errors.MailjetException
import com.mailjet.client.transactional.SendEmailsRequest
import com.mailjet.client.transactional.TransactionalEmail
import org.dataland.datalandemailservice.services.EmailSubscriptionTracker
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.StringBuilder

/**
 * A class that manages sending emails
 */
@Component
class EmailSender(
    @Autowired private val mailjetClient: MailjetClient,
    @Autowired private val emailSubscriptionTracker: EmailSubscriptionTracker,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     *  Filters the receivers and CC addresses of the given email to exclude those with the "@example.com"
     *  domain or those who are not subscribed, then sends the email via the Mailjet client based on the
     *  filtered results.
     *  @param email The email object that should be sent.
     *  @return This method does not return any value.
     */
    fun filterReceiversAndSendEmail(email: Email) {
        val filteredReceivers = email.receivers.filter(emailSubscriptionTracker::shouldSendToEmailContact)
        val filteredCc = email.cc?.filter(emailSubscriptionTracker::shouldSendToEmailContact)
        val blockedReceivers = email.receivers.filterNot(emailSubscriptionTracker::shouldSendToEmailContact)
        val blockedCc = email.cc?.filterNot(emailSubscriptionTracker::shouldSendToEmailContact)
        val blockedContacts = blockedReceivers + blockedCc
        if (blockedContacts.isNotEmpty()) {
            println("Did not send email to the following blocked contacts: $blockedContacts")
        }
        if (filteredReceivers.isEmpty() && filteredCc.isNullOrEmpty()) {
            logger.info("No email was sent. After filtering the receivers none remained.")
            return
        }
        sendEmail(
            Email(
                email.sender,
                filteredReceivers,
                filteredCc,
                email.content,
            ),
        )
    }

    /** This method sends an email
     * @param email the email to send
     * @return a sending success indicator which is true if the sending was successful
     */
    private fun sendEmail(email: Email) {
        try {
            logEmail(email)
            val mailjetEmail = TransactionalEmail.builder().integrateEmailIntoTransactionalEmailBuilder(email).build()
            val request = SendEmailsRequest.builder().message(mailjetEmail).build()
            val response = request.sendWith(mailjetClient)
            response.messages.forEach { logger.info(it.toString()) }
            logger.info("Email successfully sent.")
        } catch (e: MailjetException) {
            logger.error("Error sending email, with error: $e")
        }
    }

    private fun logEmail(email: Email) {
        val emailLog =
            StringBuilder()
                .append("Sending email with subject \"${email.content.subject}\"\n")
                .append("(sender: ${email.sender.emailAddress})\n")
                .append("(receivers: ${convertListOfEmailContactsToJoinedString(email.receivers)})")
                .apply {
                    if (!email.cc.isNullOrEmpty()) {
                        append("\n(cc receivers: ${convertListOfEmailContactsToJoinedString(email.cc)})")
                    }
                }.toString()
        logger.info(emailLog)
    }

    private fun convertListOfEmailContactsToJoinedString(emailContacts: List<EmailContact>): String =
        emailContacts.joinToString(", ") { emailContact ->
            emailContact.emailAddress
        }
}
