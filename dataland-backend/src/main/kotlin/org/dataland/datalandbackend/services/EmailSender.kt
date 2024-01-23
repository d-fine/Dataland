package org.dataland.datalandbackend.services

import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.errors.MailjetException
import com.mailjet.client.transactional.SendEmailsRequest
import com.mailjet.client.transactional.TransactionalEmail
import org.dataland.datalandbackend.model.email.Email
import org.dataland.datalandbackend.model.email.integrateEmailIntoTransactionalEmailBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * A class that manages sending emails
 */
@Component
class EmailSender(
    @Value("\${mailjet.api.url}") private val mailjetApiUrl: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val clientOptions = ClientOptions.builder()
        .baseUrl(mailjetApiUrl)
        .apiKey(System.getenv("MAILJET_API_ID"))
        .apiSecretKey(System.getenv("MAILJET_API_SECRET"))
        .build()
    private val mailjetClient = MailjetClient(clientOptions)

    /** This method sends an email
     * @param email the email to send
     * @param logMessage a message that will be logged and shall contain information about the contents
     * @return a sending success indicator which is true if the sending was successful
     */
    fun sendEmail(email: Email, loggerFunction: () -> Unit): Boolean {
        return try {
            loggerFunction()
            val mailjetEmail = TransactionalEmail.builder().integrateEmailIntoTransactionalEmailBuilder(email).build()
            val request = SendEmailsRequest.builder().message(mailjetEmail).build()
            val response = request.sendWith(mailjetClient)
            response.messages.forEach { logger.info(it.toString()) }
            logger.info("Email successfully sent.")
            true
        } catch (e: MailjetException) {
            logger.error("Error sending email, with error: $e")
            false
        }
    }
}
