package org.dataland.datalandbackend.services

import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.resource.Emailv31
import org.dataland.datalandbackend.model.email.Email
import org.json.JSONArray
import org.slf4j.LoggerFactory
import javax.management.ServiceNotFoundException

/**
 * A class that manages sending emails
 */
class EmailSender(
    private val mailServerUrl: String = "https://api.eu.mailjet.com"
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val clientOptions = ClientOptions.builder()
        .baseUrl(mailServerUrl)
        .apiKey(System.getenv("MAILJET_API_ID"))
        .apiSecretKey(System.getenv("MAILJET_API_SECRET"))
        .build()

    /** This methods sends an email
     * @param email the email to send
     */
    fun sendEmail(email: Email) {
        val client = MailjetClient(clientOptions)

        val request = MailjetRequest(Emailv31.resource)
            .property(
                Emailv31.MESSAGES,
                JSONArray().put(email.toJson())
            )
        val response = client.post(request)
        if (response.status != 200) {
            throw ServiceNotFoundException("There are problems with the email server. ${response.data.toString()}") // TODO refine this
        }
    }
}
