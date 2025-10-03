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
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

private const val USER_A = "User-a"
private const val EMAIL_ADDRESS_A = "1@example.com"
private const val EMAIL_ADDRESS_B = "2@example.com"
private const val EMAIL_ADDRESS_C = "3@example.com"
private const val EMAIL_ADDRESS_D = "a@example.com"
private const val EMAIL_ADDRESS_GENERAL_ADDITIONAL_BCC = "general_additional_bcc@example.com"
private const val EMAIL_ADDRESS_SUMMARY_ADDITIONAL_BCC = "summary_additional_bcc@example.com"

class EmailMessageListenerTest {
    private val mockEmailSender = mock<EmailSender>()
    private val objectMapper = jacksonObjectMapper()
    private val mockEmailContactService = mock<EmailContactService>()
    private val mockEmailSubscriptionTracker = mock<EmailSubscriptionTracker>()
    private val dummyProxyPrimaryUrl = "abc.example.com"
    private val testData = TypedEmailContentTestData()

    private val recipientToContactMap =
        mapOf(
            EmailRecipient.EmailAddress(EMAIL_ADDRESS_A) to EmailContact(EMAIL_ADDRESS_A),
            EmailRecipient.Internal to EmailContact(EMAIL_ADDRESS_B),
            EmailRecipient.EmailAddress(EMAIL_ADDRESS_C) to EmailContact(EMAIL_ADDRESS_C),
            EmailRecipient.UserId(USER_A) to EmailContact(EMAIL_ADDRESS_D),
            EmailRecipient.EmailAddress(EMAIL_ADDRESS_GENERAL_ADDITIONAL_BCC) to
                EmailContact(
                    EMAIL_ADDRESS_GENERAL_ADDITIONAL_BCC,
                ),
            EmailRecipient.EmailAddress(EMAIL_ADDRESS_SUMMARY_ADDITIONAL_BCC) to
                EmailContact(
                    EMAIL_ADDRESS_SUMMARY_ADDITIONAL_BCC,
                ),
        )

    private val senderContact = EmailContact("sender@example.com")

    private val contactToSubscriptionStatusMap =
        mapOf(
            EmailContact(EMAIL_ADDRESS_A) to Pair(true, UUID.randomUUID()),
            EmailContact(EMAIL_ADDRESS_B) to Pair(false, UUID.randomUUID()),
            EmailContact(EMAIL_ADDRESS_C) to Pair(true, UUID.randomUUID()),
            EmailContact(EMAIL_ADDRESS_D) to Pair(false, UUID.randomUUID()),
            EmailContact(EMAIL_ADDRESS_GENERAL_ADDITIONAL_BCC) to Pair(true, UUID.randomUUID()),
            EmailContact(EMAIL_ADDRESS_SUMMARY_ADDITIONAL_BCC) to Pair(true, UUID.randomUUID()),
        )

    private val correlationId = "correlationId"
    private val receiver =
        listOf(
            EmailRecipient.EmailAddress(EMAIL_ADDRESS_A),
            EmailRecipient.Internal,
            EmailRecipient.EmailAddress(EMAIL_ADDRESS_C),
            EmailRecipient.UserId(USER_A),
        )
    private val cc = listOf(EmailRecipient.EmailAddress(EMAIL_ADDRESS_C), EmailRecipient.UserId(USER_A))
    private val bcc = listOf(EmailRecipient.Internal, EmailRecipient.UserId(USER_A))

    private val allowedReceiver = listOf(EmailContact(EMAIL_ADDRESS_A), EmailContact(EMAIL_ADDRESS_C))
    private val allowedCc = listOf(EmailContact(EMAIL_ADDRESS_C))
    private val additionalBccForNonSummaryEmail = listOf(EmailContact(EMAIL_ADDRESS_GENERAL_ADDITIONAL_BCC))
    private val allowedBcc =
        listOf(
            EmailContact(EMAIL_ADDRESS_GENERAL_ADDITIONAL_BCC),
            EmailContact(EMAIL_ADDRESS_SUMMARY_ADDITIONAL_BCC),
        )

    @BeforeEach
    fun setup() {
        reset(
            mockEmailSender,
            mockEmailContactService,
            mockEmailSubscriptionTracker,
        )
        whenever(mockEmailContactService.getContacts(any())).thenAnswer { invocation ->
            val recipient: EmailRecipient = invocation.getArgument(0)
            listOf(recipientToContactMap[recipient])
        }
        doReturn(senderContact).whenever(mockEmailContactService).getSenderContact()

        whenever(mockEmailSubscriptionTracker.subscribeContactsIfNeededAndPartition(any())).thenAnswer { invocation ->
            val contacts: List<EmailContact> = invocation.getArgument(0)
            val (allowed, blocked) = contacts.partition { contactToSubscriptionStatusMap[it]?.first ?: false }
            EmailSubscriptionTracker.PartitionedContacts(
                allowed.associateWith { contactToSubscriptionStatusMap[it]?.second ?: UUID.randomUUID() },
                blocked,
            )
        }
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
    fun `test that correct email about access request is sent to correct contacts`() {
        val typedEmailContent = testData.accessToDatasetRequestedEmailContent
        val keywords = testData.accessToDatasetRequestedKeywords.toMutableList()
        keywords.remove(TypedEmailContentTestData.BASE_URL)
        keywords.add("https://$dummyProxyPrimaryUrl")
        val jsonString = objectMapper.writeValueAsString(EmailMessage(typedEmailContent, receiver, cc, bcc))
        doNothing().whenever(mockEmailSender).sendEmail(any())

        val emailMessageListener =
            EmailMessageListener(
                mockEmailSender,
                mockEmailContactService,
                mockEmailSubscriptionTracker,
                dummyProxyPrimaryUrl,
                true,
                "  $EMAIL_ADDRESS_GENERAL_ADDITIONAL_BCC;;",
                EMAIL_ADDRESS_SUMMARY_ADDITIONAL_BCC,
            )
        emailMessageListener.handleSendEmailMessage(jsonString, MessageType.SEND_EMAIL, correlationId)

        val emailCaptor = argumentCaptor<Email>()

        verify(mockEmailSender).sendEmail(emailCaptor.capture())

        val sentEmail = emailCaptor.firstValue

        assertSenderReceiverCcAndBcc(sentEmail, allowedReceiver, allowedCc, additionalBccForNonSummaryEmail)
        assert(
            keywords.all { keyword ->
                sentEmail.content.htmlContent.contains(keyword) &&
                    sentEmail.content.textContent.contains(keyword)
            },
        )
    }

    @Test
    fun `test that correct data request summary email is sent to correct contacts`() {
        val typedEmailContent = testData.dataRequestSummaryEmailContent
        val keywords = testData.dataRequestSummaryKeywords
        val jsonString = objectMapper.writeValueAsString(EmailMessage(typedEmailContent, receiver, cc, bcc))
        doNothing().whenever(mockEmailSender).sendEmail(any())

        val emailMessageListener =
            EmailMessageListener(
                mockEmailSender,
                mockEmailContactService,
                mockEmailSubscriptionTracker,
                dummyProxyPrimaryUrl,
                true,
                "  $EMAIL_ADDRESS_GENERAL_ADDITIONAL_BCC;;",
                EMAIL_ADDRESS_SUMMARY_ADDITIONAL_BCC,
            )
        emailMessageListener.handleSendEmailMessage(jsonString, MessageType.SEND_EMAIL, correlationId)

        val emailCaptor = argumentCaptor<Email>()

        verify(mockEmailSender).sendEmail(emailCaptor.capture())

        val sentEmail = emailCaptor.firstValue

        assertSenderReceiverCcAndBcc(sentEmail, allowedReceiver, allowedCc, allowedBcc)
        assert(
            keywords.all { keyword ->
                sentEmail.content.htmlContent.contains(keyword) &&
                    sentEmail.content.textContent.contains(keyword)
            },
        )
    }

    @Test
    fun `test that correct email is sent to correct contacts with correct subscription uuid`() {
        val recipient = recipientToContactMap.keys.first()
        val receiver = listOf(recipient)
        val receiverContact = EmailContact.create(EMAIL_ADDRESS_A)
        val typedEmailContent = testData.dataNonSourceableEmailContent
        val keywords = testData.dataNonSourceableKeywords.toMutableList()
        keywords.remove(TypedEmailContentTestData.BASE_URL)
        keywords.add("https://$dummyProxyPrimaryUrl")
        keywords.remove(testData.subscriptionUuid)
        val jsonString =
            objectMapper.writeValueAsString(
                EmailMessage(typedEmailContent, receiver, emptyList(), emptyList()),
            )
        doNothing().whenever(mockEmailSender).sendEmail(any())

        val emailMessageListener =
            EmailMessageListener(
                mockEmailSender,
                mockEmailContactService,
                mockEmailSubscriptionTracker,
                dummyProxyPrimaryUrl,
                true,
                "",
                "",
            )
        emailMessageListener.handleSendEmailMessage(jsonString, MessageType.SEND_EMAIL, correlationId)

        val emailCaptor = argumentCaptor<Email>()

        verify(mockEmailSender).sendEmail(emailCaptor.capture())

        val sentEmail = emailCaptor.firstValue

        assertSenderReceiverCcAndBcc(sentEmail, listOf(receiverContact), emptyList(), emptyList())
        assert(
            keywords.all { keyword ->
                sentEmail.content.htmlContent.contains(keyword) &&
                    sentEmail.content.textContent.contains(keyword)
            },
        )
    }
}
