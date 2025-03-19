package org.dataland.datalandemailservice.email

import com.mailjet.client.MailjetClient
import com.mailjet.client.errors.MailjetException
import com.mailjet.client.transactional.SendEmailsRequest
import com.mailjet.client.transactional.TransactionalEmail
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * A class that manages sending emails
 */
@Component
class EmailSender(
    @Autowired private val mailjetClient: MailjetClient,
    @Value("\${dataland.email.service.dry.run}") private val dryRunIsActive: Boolean,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /** This method sends an email. Note this function does not filter the email addresses.
     * @param email the email to send
     * @return a sending success indicator which is true if the sending was successful
     */
    fun sendEmail(email: Email) {
        if (dryRunIsActive) {
            logEmailInDryRun(email)
        } else {
            try {
                logEmail(email)
                val mailjetEmail =
                    TransactionalEmail.builder().integrateEmailIntoTransactionalEmailBuilder(email).build()
                val request = SendEmailsRequest.builder().message(mailjetEmail).build()
                val response = request.sendWith(mailjetClient)
                response.messages.forEach { logger.info(it.toString()) }
                logger.info("Email successfully sent.")
            } catch (e: MailjetException) {
                logger.error("Error sending email, with error: $e")
            }
        }
    }

    private fun emailDescriptionForLogMessage(email: Email): String =
        buildString {
            append("email with subject \"${email.content.subject}\"\n")
            append("(sender: ${email.sender.emailAddress})\n")
            append("(receivers: ${convertListOfEmailContactsToJoinedString(email.receivers)})")

            if (email.cc.isNotEmpty()) {
                append("\n(cc receivers: ${convertListOfEmailContactsToJoinedString(email.cc)})")
            }

            if (email.bcc.isNotEmpty()) {
                append("\n(bcc receivers: ${convertListOfEmailContactsToJoinedString(email.bcc)})")
            }
        }

    private fun logEmail(email: Email) {
        val emailLog =
            buildString {
                append("Sending ")
                append(emailDescriptionForLogMessage(email))
            }

        logger.info(emailLog)
    }

    private fun logEmailInDryRun(email: Email) {
        val emailLog =
            buildString {
                append("Withholding ")
                append(emailDescriptionForLogMessage(email))
                append("\ndue to email service dry run!")
            }

        logger.info(emailLog)
    }

    private fun convertListOfEmailContactsToJoinedString(emailContacts: List<EmailContact>): String =
        emailContacts.joinToString(", ") { emailContact ->
            emailContact.emailAddress
        }
}
