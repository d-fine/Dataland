package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandemailservice.email.Email
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

private const val USER_A = "User-a"
private const val EMAIL_ADDRESS_A = "1@example.com"
private const val EMAIL_ADDRESS_B = "2@example.com"
private const val EMAIL_ADDRESS_C = "3@example.com"
private const val EMAIL_ADDRESS_D = "a@example.com"

class EmailMessageListenerTest {
    private lateinit var emailSender: EmailSender
    private var objectMapper = jacksonObjectMapper()
    private lateinit var emailContactService: EmailContactService
    private lateinit var emailSubscriptionTracker: EmailSubscriptionTracker
    private val proxyPrimaryUrl = "abc.example.com"
    private val dryRunIsActive = false

    private lateinit var emailMessageListener: EmailMessageListener
    private lateinit var testData: TypedEmailContentTestData

    private val recipientToContactMap =
        mapOf(
            EmailRecipient.EmailAddress(EMAIL_ADDRESS_A) to EmailContact(EMAIL_ADDRESS_A),
            EmailRecipient.Internal to EmailContact(EMAIL_ADDRESS_B),
            EmailRecipient.EmailAddress(EMAIL_ADDRESS_C) to EmailContact(EMAIL_ADDRESS_C),
            EmailRecipient.UserId(USER_A) to EmailContact(EMAIL_ADDRESS_D),
        )

    private val senderContact = EmailContact("sender@example.com")

    private val contactToSubscriptionStatusMap =
        mapOf(
            EmailContact(EMAIL_ADDRESS_A) to Pair(true, UUID.randomUUID()),
            EmailContact(EMAIL_ADDRESS_B) to Pair(false, UUID.randomUUID()),
            EmailContact(EMAIL_ADDRESS_C) to Pair(true, UUID.randomUUID()),
            EmailContact(EMAIL_ADDRESS_D) to Pair(false, UUID.randomUUID()),
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
        `when`(emailSubscriptionTracker.subscribeContactsIfNeededAndPartition(any())).thenAnswer { invocation ->
            val contacts: List<EmailContact> = invocation.getArgument(0)
            val (allowed, blocked) = contacts.partition { contactToSubscriptionStatusMap[it]?.first ?: false }
            EmailSubscriptionTracker.PartitionedContacts(
                allowed.associateWith { contactToSubscriptionStatusMap[it]?.second ?: UUID.randomUUID() },
                blocked,
            )
        }

        emailMessageListener =
            EmailMessageListener(
                emailSender, objectMapper, emailContactService, emailSubscriptionTracker, proxyPrimaryUrl, dryRunIsActive,
            )

        testData = TypedEmailContentTestData()
    }

    private fun assertSenderReceiverCcAndBcc(
        email: Email,
        receiver: List<EmailContact>,
        cc: List<EmailContact>,
        bcc: List<EmailContact>,
    ) = email.sender == senderContact &&
        email.receivers == receiver &&
        email.cc == cc &&
        email.bcc == bcc

    @Test
    fun `test that correct email is send to correct contacts`() {
        val receiver = recipientToContactMap.keys.toList()
        val cc = listOf(EmailRecipient.EmailAddress(EMAIL_ADDRESS_C), EmailRecipient.UserId(USER_A))
        val bcc = listOf(EmailRecipient.Internal, EmailRecipient.UserId(USER_A))

        val allowedReceiver = listOf(EmailContact(EMAIL_ADDRESS_A), EmailContact(EMAIL_ADDRESS_C))
        val allowedCc = listOf(EmailContact(EMAIL_ADDRESS_C))

        val typedEmailContent = testData.accessToDatasetRequested
        val keywords = testData.accessToDatasetRequestedKeywords.toMutableList()
        keywords.remove(TypedEmailContentTestData.BASE_URL)
        keywords.add("https://$proxyPrimaryUrl")

        val jsonString = objectMapper.writeValueAsString(EmailMessage(typedEmailContent, receiver, cc, bcc))

        doNothing().whenever(emailSender).sendEmail(any())

        emailMessageListener.handleSendEmailMessage(jsonString, MessageType.SEND_EMAIL, correlationId)

        verify(emailSender).sendEmail(
            argThat { email ->
                assertSenderReceiverCcAndBcc(email, allowedReceiver, allowedCc, emptyList()) &&
                    keywords.all { keyword ->
                        email.content.htmlContent.contains(keyword) && email.content.textContent.contains(keyword)
                    }
            },
        )
    }

    @Test
    fun `test that correct email is send to correct contacts with correct subscription uuid`() {
        val recipient = recipientToContactMap.keys.first()
        val receiver = listOf(recipient)
        val receiverContact = EmailContact.create(EMAIL_ADDRESS_A)

        val typedEmailContent = testData.singleDatasetUploadedEngagement

        val keywords = testData.singleDatasetUploadedEngagementKeywords.toMutableList()
        keywords.remove(TypedEmailContentTestData.BASE_URL)
        keywords.add("https://$proxyPrimaryUrl")
        keywords.remove(testData.subscriptionUuid)
        contactToSubscriptionStatusMap[receiverContact]
        keywords.add(contactToSubscriptionStatusMap[receiverContact]?.second.toString())

        val jsonString =
            objectMapper.writeValueAsString(
                EmailMessage(typedEmailContent, receiver, emptyList(), emptyList()),
            )

        doNothing().whenever(emailSender).sendEmail(any())

        emailMessageListener.handleSendEmailMessage(jsonString, MessageType.SEND_EMAIL, correlationId)

        verify(emailSender).sendEmail(
            argThat { email ->
                assertSenderReceiverCcAndBcc(email, listOf(receiverContact), emptyList(), emptyList()) &&
                    keywords.all { keyword ->
                        email.content.htmlContent.contains(keyword) && email.content.textContent.contains(keyword)
                    }
            },
        )
    }
}
