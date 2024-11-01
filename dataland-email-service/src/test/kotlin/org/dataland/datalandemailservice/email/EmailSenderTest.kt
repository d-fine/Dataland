package org.dataland.datalandemailservice.email
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetResponse
import org.dataland.datalandemailservice.services.EmailUnsubscriber
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
    private lateinit var mockEmailSubscriptionService: EmailUnsubscriber

    private val unsubscribedEmailAddress = "abc@123.com"
    private val missingEmailAddress = "xyz@123.com"
    private val subscribedEmailAddress = "def@123.com"

    @BeforeEach
    fun setup() {
        mockMailjetClient = mock(MailjetClient::class.java)
        `when`(mockMailjetClient.post(any())).thenReturn(MailjetResponse(200, "{}"))
        mockEmailSubscriptionService = mock(EmailUnsubscriber::class.java)
        `when`(mockEmailSubscriptionService.emailIsSubscribed(unsubscribedEmailAddress)).thenReturn(false)
        `when`(mockEmailSubscriptionService.emailIsSubscribed(missingEmailAddress)).thenReturn(null)
        `when`(mockEmailSubscriptionService.emailIsSubscribed(subscribedEmailAddress)).thenReturn(true)
        emailSender = EmailSender(mockMailjetClient, mockEmailSubscriptionService)
    }

    @Test
    fun `check if the mail is send to subscribed email address`() {
        val receiver = EmailContact(subscribedEmailAddress)
        val email =
            Email(
                senderContact, listOf(receiver),
                listOf(), emailContent,
            )
        emailSender.filterReceiversAndSentEmail(email)
        verify(mockMailjetClient, times(1)).post(any())
    }

    @Test
    fun `check if the mail is send to email address not stored in the database`() {
        val receiver = EmailContact(missingEmailAddress)
        val email =
            Email(
                senderContact, listOf(receiver),
                listOf(), emailContent,
            )
        emailSender.filterReceiversAndSentEmail(email)
        verify(mockMailjetClient, times(1)).post(any())
    }

    @Test
    fun `check that email is not send to not subscribed email address`() {
        val receiver = EmailContact(unsubscribedEmailAddress)
        val email =
            Email(
                senderContact, listOf(receiver),
                listOf(), emailContent,
            )
        emailSender.filterReceiversAndSentEmail(email)
        verify(mockMailjetClient, times(0)).post(any())
    }

    @Test
    fun `check that email is not send to example domain`() {
        val cc = EmailContact("CC@example.comn")

        val email = Email(senderContact, emptyList(), listOf(cc), emailContent)
        emailSender.filterReceiversAndSentEmail(email)
        verify(mockMailjetClient, times(0)).post(any())
    }
}
