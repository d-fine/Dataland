package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandemailservice.email.TypedEmailContentTestData
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class EmailMessageListenerTest {
    private lateinit var emailSender: EmailSender
    private var objectMapper = jacksonObjectMapper()
    private lateinit var emailContactService: EmailContactService
    private lateinit var emailSubscriptionTracker: EmailSubscriptionTracker
    private val proxyPrimaryUrl = "abc.example.com"

    private lateinit var emailMessageListener: EmailMessageListener

    private val recipientToContactMap =
        mapOf(
            EmailRecipient.EmailAddress("1@example.com") to EmailContact("1@example.com"),
            EmailRecipient.Internal to EmailContact("2@example.com"),
            EmailRecipient.EmailAddress("3@example.com") to EmailContact("3@example.com"),
            EmailRecipient.UserId("User-a") to EmailContact("a@example.com"),
        )

    private val senderContact = EmailContact("sender@example.com")

    private val contactToSubscriptionStatusMap =
        mapOf(
            EmailContact("1@example.com") to Pair(true, UUID.randomUUID()),
            EmailContact("2@example.com") to Pair(false, UUID.randomUUID()),
            EmailContact("3@example.com") to Pair(true, UUID.randomUUID()),
            EmailContact("a@example.com") to Pair(false, UUID.randomUUID()),
        )

    private val correlationId = "correlationId"

    @BeforeEach
    fun setup() {
        emailSender = mock(EmailSender::class.java)
        emailContactService = mock(EmailContactService::class.java)
        `when`(emailContactService.getContacts(any())).thenAnswer { invocation ->
            val recipient: EmailRecipient = invocation.getArgument(0)
            listOf(recipientToContactMap[recipient])
        }
        `when`(emailContactService.getSenderContact()).thenReturn(senderContact)

        emailSubscriptionTracker = mock(EmailSubscriptionTracker::class.java)
        `when`(emailSubscriptionTracker.subscribeContactsIfNeededAndFilter(any())).thenAnswer { invocation ->
            val contacts: List<EmailContact> = invocation.getArgument(0)
            val (allowed, blocked) = contacts.partition { contactToSubscriptionStatusMap[it]!!.first }
            EmailSubscriptionTracker.FilteredContacts(
                allowed.associateWith { contactToSubscriptionStatusMap[it]!!.second },
                blocked,
            )
        }

        emailMessageListener =
            EmailMessageListener(
                emailSender, objectMapper, emailContactService, emailSubscriptionTracker, proxyPrimaryUrl,
            )
    }

    @Test
    fun `test that correct email is send to correct contacts`() {
        val receiver = recipientToContactMap.keys.toList()
        val cc = listOf(EmailRecipient.EmailAddress("3@example.com"), EmailRecipient.UserId("User-a"))
        val bcc = listOf(EmailRecipient.Internal, EmailRecipient.UserId("User-a"))

        val allowedReceiver = listOf(EmailContact("1@example.com"), EmailContact("3@example.com"))
        val allowedCc = listOf(EmailContact("3@example.com"))

        val typedEmailContent = TypedEmailContentTestData.accessToDatasetRequested
        val keywords = TypedEmailContentTestData.accessToDatasetRequestedKeywords.toMutableList()
        keywords.remove(TypedEmailContentTestData.baseUrl)
        keywords.add(proxyPrimaryUrl)

        val jsonString = objectMapper.writeValueAsString(EmailMessage(typedEmailContent, receiver, cc, bcc))

        doNothing().whenever(emailSender).sendEmail(any())

        emailMessageListener.handleSendEmailMessage(jsonString, MessageType.SEND_EMAIL, correlationId)

        verify(emailSender).sendEmail(
            argThat { email ->
                email.sender == senderContact &&
                    email.receivers == allowedReceiver &&
                    email.cc == allowedCc &&
                    email.bcc.isEmpty() &&
                    keywords.all { keyword ->
                        email.content.htmlContent.contains(keyword) && email.content.textContent.contains(keyword)
                    }
            },
        )
    }

    @Test
    fun `test that correct email is send to correct contacts with correct subscription uuid`() {
        val receiver = listOf(recipientToContactMap.keys.first())

        val allowed = listOf(EmailContact("1@example.com"))

        val typedEmailContent = TypedEmailContentTestData.singleDatasetUploadedEngagement
        val keywords = TypedEmailContentTestData.singleDatasetUploadedEngagementKeywords.toMutableList()
        keywords.remove(TypedEmailContentTestData.baseUrl)
        keywords.add(proxyPrimaryUrl)
        keywords.remove(TypedEmailContentTestData.subscriptionUuid)
        keywords.add(contactToSubscriptionStatusMap[allowed.first()]!!.second.toString())

        val jsonString =
            objectMapper.writeValueAsString(
                EmailMessage(typedEmailContent, receiver, emptyList(), emptyList()),
            )

        doNothing().whenever(emailSender).sendEmail(any())

        emailMessageListener.handleSendEmailMessage(jsonString, MessageType.SEND_EMAIL, correlationId)

        verify(emailSender).sendEmail(
            argThat { email ->
                email.sender == senderContact &&
                    email.receivers == allowed &&
                    email.cc.isEmpty() &&
                    email.bcc.isEmpty() &&
                    keywords.all { keyword ->
                        email.content.htmlContent.contains(keyword) && email.content.textContent.contains(keyword)
                    }
            },
        )
    }
}
