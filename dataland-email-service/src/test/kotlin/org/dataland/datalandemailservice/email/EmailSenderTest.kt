package org.dataland.datalandemailservice.email
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class EmailSenderTest {
    private class EmailSendException : RuntimeException()

    private val senderContact = EmailContact("sender@example.com")
    private val receiversContact = EmailContact("receiver@example.com")
    private val mockMailjetClient = mock(MailjetClient::class.java)
    private val mockMailjetRequest = mock(MailjetRequest::class.java)

    private fun callEmailSender(): EmailSender {
        `when`(mockMailjetClient.post(any() ?: mockMailjetRequest)).thenThrow(EmailSendException())
        val emailSender = EmailSender(mockMailjetClient)
        return emailSender
    }

    @Test
    fun `check if the mail sender works as expected`() {
        val senderContact = EmailContact("sender@dataland.com")
        val email =
            Email(
                senderContact, listOf(senderContact),
                listOf(), EmailContent("", "", ""),
            )
        val emailSender = callEmailSender()
        assertThrows<EmailSendException> {
            emailSender.sendEmailWithoutTestReceivers(email)
        }
    }

    @Test
    fun `check if the suppressing of test receivers works as expected  `() {
        val senderCC = EmailContact("CC@somethingelse.comn")
        val email =
            Email(
                senderContact, listOf(receiversContact), listOf(senderCC),
                EmailContent("", "", ""),
            )
        val emailSender = callEmailSender()
        assertThrows<EmailSendException> {
            emailSender.sendEmailWithoutTestReceivers(email)
        }
    }

    @Test
    fun `check if the suppressing of test receivers and carbon copy works as expected`() {
        val senderCC = EmailContact("CC@example.comn")
        val email =
            Email(
                senderContact, listOf(receiversContact), listOf(senderCC),
                EmailContent("", "", ""),
            )
        val emailSender = callEmailSender()
        assertDoesNotThrow {
            emailSender.sendEmailWithoutTestReceivers(email)
        }
    }
}
