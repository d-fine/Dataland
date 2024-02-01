package org.dataland.datalandbackend.email

import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import org.dataland.datalandemail.email.Email
import org.dataland.datalandemail.email.EmailContact
import org.dataland.datalandemail.email.EmailContent
import org.dataland.datalandemail.email.EmailSender
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class EmailSenderTest {
    private class EmailSendException : Exception()

    @Test
    fun `check if the mail sender works as expected`() {
        val senderContact = EmailContact("sender@dataland.com")
        val email = Email(senderContact, listOf(senderContact), listOf(), EmailContent("", "", ""))
        val mockMailjetClient = mock(MailjetClient::class.java)
        val mockMailjetRequest = mock(MailjetRequest::class.java)
        `when`(mockMailjetClient.post(any() ?: mockMailjetRequest)).thenThrow(EmailSendException())
        val emailSender = EmailSender(mockMailjetClient)
        assertThrows<EmailSendException> {
            emailSender.sendEmail(email)
        }
    }
}
