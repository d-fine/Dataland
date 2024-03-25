package org.dataland.datalandemailservice.email
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.slf4j.LoggerFactory

class EmailSenderTest {
    private class EmailSendException : RuntimeException()
    private val logger = LoggerFactory.getLogger(javaClass)

    @Test
    fun `check if the mail sender works as expected`() {
        val senderContact = EmailContact("sender@dataland.com")
        val email = Email(senderContact, listOf(senderContact), listOf(), EmailContent("", "", ""))
        val mockMailjetClient = mock(MailjetClient::class.java)
        val mockMailjetRequest = mock(MailjetRequest::class.java)
        `when`(mockMailjetClient.post(any() ?: mockMailjetRequest)).thenThrow(EmailSendException())
        val emailSender = EmailSender(mockMailjetClient)
        assertThrows<EmailSendException> {
            emailSender.sendEmailWithoutTestReceivers(email)
        }
    }

    @Test
    fun `check if the suppressing of test receivers works as expected  `() {
        val senderContact = EmailContact("sender@dataland.com")
        val receiversContact = EmailContact("receiver@example.com")
        val senderCC = EmailContact("CC@somethingelse.com")
        val email = Email(
            senderContact, listOf(receiversContact), listOf(senderCC),
            EmailContent("", "", ""),
        )
        val mockMailjetClient = mock(MailjetClient::class.java)
        val mockMailjetRequest = mock(MailjetRequest::class.java)
        `when`(mockMailjetClient.post(any() ?: mockMailjetRequest)).thenThrow(EmailSendException())
        val emailSender = EmailSender(mockMailjetClient)
        assertThrows<EmailSendException> {
            emailSender.sendEmailWithoutTestReceivers(email)
        }
    }
}
