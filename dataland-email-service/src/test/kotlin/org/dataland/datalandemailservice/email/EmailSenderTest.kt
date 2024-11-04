package org.dataland.datalandemailservice.email
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetResponse
import org.dataland.datalandemailservice.services.EmailSubscriptionTracker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class EmailSenderTest {
    private val senderContact = EmailContact("sender@example.com")
    private val emailContent = EmailContent("", "", "")

    private lateinit var mockMailjetClient: MailjetClient
    private lateinit var emailSender: EmailSender
    private lateinit var emailSubscriptionTracker: EmailSubscriptionTracker

    private val subscribedEmailAddress = "def@123.com"
    private val unsubscribedEmailAddress = "abc@123.com"
    private val missingEmailAddress = "xyz@123.com"

    private val subscribedEmailContact = EmailContact(subscribedEmailAddress, "Subscriber")
    private val unsubscribedEmailContact = EmailContact(subscribedEmailAddress, "Unsubscriber")
    private val missingEmailContact = EmailContact(missingEmailAddress, "Unknown")

    @BeforeEach
    fun setup() {
        mockMailjetClient = mock(MailjetClient::class.java)
        `when`(mockMailjetClient.post(any())).thenReturn(MailjetResponse(200, "{}"))
        emailSubscriptionTracker = mock(EmailSubscriptionTracker::class.java)
        `when`(emailSubscriptionTracker.isEmailSubscribed(subscribedEmailAddress)).thenReturn(true)
        `when`(emailSubscriptionTracker.isEmailSubscribed(unsubscribedEmailAddress)).thenReturn(false)
        `when`(emailSubscriptionTracker.isEmailSubscribed(missingEmailAddress)).thenReturn(false)
        `when`(emailSubscriptionTracker.shouldSendToEmailContact(subscribedEmailContact)).thenReturn(true)
        `when`(emailSubscriptionTracker.shouldSendToEmailContact(unsubscribedEmailContact)).thenReturn(false)
        `when`(emailSubscriptionTracker.shouldSendToEmailContact(missingEmailContact)).thenReturn(false)
        emailSender = EmailSender(mockMailjetClient, emailSubscriptionTracker)
    }

    @Test
    fun `check if the email is sent to subscribed email address`() {
        val receiver = subscribedEmailContact
        val email =
            Email(
                senderContact, listOf(receiver),
                listOf(), emailContent,
            )
        emailSender.filterReceiversAndSendEmail(email)
        verify(mockMailjetClient, times(1)).post(any())
    }

    @Test
    fun `check that email is not send to not subscribed email address`() {
        val receiver = unsubscribedEmailContact
        val email =
            Email(
                senderContact, listOf(receiver),
                listOf(), emailContent,
            )
        emailSender.filterReceiversAndSendEmail(email)
        verify(mockMailjetClient, times(0)).post(any())
    }

    @Test
    fun `check that email is not sent to email address not stored in the database`() {
        val receiver = missingEmailContact
        val email =
            Email(
                senderContact, listOf(receiver),
                listOf(), emailContent,
            )
        emailSender.filterReceiversAndSendEmail(email)
        verify(mockMailjetClient, times(0)).post(any())
    }

    @Test
    fun `check that email is not sent to example domain`() {
        val cc = EmailContact("CC@example.comn")

        val email = Email(senderContact, emptyList(), listOf(cc), emailContent)
        emailSender.filterReceiversAndSendEmail(email)
        verify(mockMailjetClient, times(0)).post(any())
    }
}
