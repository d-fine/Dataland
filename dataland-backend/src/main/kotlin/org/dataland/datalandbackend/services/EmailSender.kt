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
    fun sendEmail(email: Email) {
        email.receivers.forEach { logger.info("Sending an email to $it.") }
        email.cc.forEach { logger.info("Sending an email with $it in cc.") }
        val mailjetEmail = TransactionalEmail.builder().email(email).build()
        val request = SendEmailsRequest.builder().message(mailjetEmail).build()
        val response = request.sendWith(client)
        response.messages.forEach { logger.info(it.toString()) }
        // TODO status is non 200 and no email is sent if either
        // TODO 1. an attachment has no content
        // TODO 2. the INVITE_REQUEST_RECEIVERS env is not set or empty
        // TODO discuss: should we throw exceptions? I think the user needs to know if an uploaded file was empty.
        // TODO an exception is already thrown if the mail server is not available
    }
}
