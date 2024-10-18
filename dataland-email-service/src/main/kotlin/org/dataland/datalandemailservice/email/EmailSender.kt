package org.dataland.datalandemailservice.email

import com.mailjet.client.MailjetClient
import com.mailjet.client.errors.MailjetException
import com.mailjet.client.transactional.SendEmailsRequest
import com.mailjet.client.transactional.TransactionalEmail
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
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /** This method checks incoming email objects if their receivers or cc property contains the domain name
     * "@example.com" and suppresses their forwarding to the mailjet client
     * @param email the email to be checked
     */
    fun sendEmailWithoutTestReceivers(email: Email) {
        val receiversWithoutExampleDomains = email.receivers.filterNot { it.emailAddress.contains("@example.com") }
        val ccWithoutExampleDomains = email.cc?.filterNot { it.emailAddress.contains("@example.com") }
        if (receiversWithoutExampleDomains.isEmpty() && !ccWithoutExampleDomains.isNullOrEmpty()) {
            sendEmail(
                Email(
                    email.sender,
                    ccWithoutExampleDomains,
                    listOf(),
                    email.content,
                ),
            )
        } else if (receiversWithoutExampleDomains.isEmpty() && ccWithoutExampleDomains.isNullOrEmpty()) {
            logger.info("No email was sent. After filtering example.com email domain no recipients remain.")
        } else {
            sendEmail(email)
        }
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
