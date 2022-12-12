package org.dataland.datalandbackend.services

import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.transactional.SendContact
import com.mailjet.client.transactional.SendEmailsRequest
import com.mailjet.client.transactional.TransactionalEmail
import org.dataland.datalandbackend.model.email.Email
import org.dataland.datalandbackend.model.email.EmailContent
import org.dataland.datalandbackend.model.email.email

/**
 * A class that manages sending emails
 */
class EmailSender {
    private val mailServerUrl: String = "https://api.eu.mailjet.com"
    private val clientOptions = ClientOptions.builder()
        .baseUrl(mailServerUrl)
        .apiKey(System.getenv("MAILJET_API_ID"))
        .apiSecretKey(System.getenv("MAILJET_API_SECRET"))
        .build()
    private val client = MailjetClient(clientOptions)

    private val infoSender = SendContact("info@dataland.com", "Dataland")

    /** This methods sends an email
     * @param email the email to send
     */
    fun sendEmail(email: Email) {
        val mailjetEmail = TransactionalEmail.builder().email(email).build()
        val request = SendEmailsRequest.builder().message(mailjetEmail).build()
        request.sendWith(client)
    }

    fun sendInfoEmail(receiver: SendContact, content: EmailContent) {
        val email = Email(infoSender, receiver, content)
        sendEmail(email)
    }
}
