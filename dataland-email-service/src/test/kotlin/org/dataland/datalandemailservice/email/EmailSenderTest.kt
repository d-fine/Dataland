package org.dataland.datalandemailservice.email

import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class EmailSenderTest {
    private class EmailSendException : RuntimeException()

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
