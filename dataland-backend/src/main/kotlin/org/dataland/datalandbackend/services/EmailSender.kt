package org.dataland.datalandbackend.services

import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.transactional.SendEmailsRequest
import com.mailjet.client.transactional.TransactionalEmail
import org.dataland.datalandbackend.model.email.Email
import org.dataland.datalandbackend.model.email.email
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * A class that manages sending emails
 */
@Component
class EmailSender {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val mailServerUrl: String = "https://api.eu.mailjet.com"
    private val clientOptions = ClientOptions.builder()
        .baseUrl(mailServerUrl)
        .apiKey(System.getenv("MAILJET_API_ID"))
        .apiSecretKey(System.getenv("MAILJET_API_SECRET"))
        .build()
    private val client = MailjetClient(clientOptions)

    /** This methods sends an email
     * @param email the email to send
     */
    fun sendEmail(email: Email): Boolean {
        try {
            email.receivers.forEach { logger.info("Sending an email to $it.") }
            email.cc.forEach { logger.info("Sending an email with $it in cc.") }
            val mailjetEmail = TransactionalEmail.builder().email(email).build()
            val request = SendEmailsRequest.builder().message(mailjetEmail).build()
            val response = request.sendWith(client)
            response.messages.forEach { logger.info(it.toString()) }}
        catch (e:Exception) {
            return false
        }
        return true
    }
}
