package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.aspectj.bridge.Message
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandemailservice.repositories.EmailSubscriptionRepository
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.messages.email.DatasetRequestedClaimOwnership
import org.dataland.datalandmessagequeueutils.messages.email.EmailAddressRecipient
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.InternalRecipients
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailData
import org.dataland.datalandmessagequeueutils.messages.email.UserIdRecipient
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import java.util.*

class EmailMessageListenerTest {

    private lateinit var emailSender: EmailSender
    private val messageQueueUtils = MessageQueueUtils()
    private var objectMapper  = jacksonObjectMapper()
    private lateinit var emailContactService: EmailContactService
    private lateinit var emailSubscriptionTracker: EmailSubscriptionTracker
    private val proxyPrimaryUrl = "abc.example.com"

    private lateinit var emailMessageListener: EmailMessageListener

    private val testDatasetRequestedClaimOwnership = DatasetRequestedClaimOwnership(
        "companyId", "companyName", "requesterEmail", "dataType", listOf("2020", "2023"),
        "message", "firstName", "lastName"
    )

    private val typedEmailTestData: List<TypedEmailData> = listOf(testDatasetRequestedClaimOwnership)

    private val recipientToContactMap = mapOf(
        EmailAddressRecipient("1@example.com") to EmailContact("1@example.com"),
        InternalRecipients to EmailContact("2@example.com"),
        EmailAddressRecipient("3@example.com") to EmailContact("3@example.com"),
        UserIdRecipient("User-a") to EmailContact("a@example.com"),
    )

    private val senderContact = EmailContact("sender@example.com")

    private val contactToSubscriptionStatusMap = mapOf(
        EmailContact("1@example.com") to Pair(true, UUID.randomUUID()),
        EmailContact("2@example.com") to Pair(false, UUID.randomUUID()),
        EmailContact("3@example.com") to Pair(true, UUID.randomUUID()),
        EmailContact("a@example.com") to Pair(false, UUID.randomUUID()),
    )

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
        `when`(emailSubscriptionTracker.filterContacts(any())).thenAnswer { invocation ->
            val contacts: List<EmailContact> = invocation.getArgument(0)
            val (allowed, blocked) = contacts.partition { contactToSubscriptionStatusMap[it]!!.first }
            EmailSubscriptionTracker.FilteredContacts(
                allowed.associateWith { contactToSubscriptionStatusMap[it]!!.second },
                blocked
            )
        }

        emailMessageListener = EmailMessageListener(
            emailSender, messageQueueUtils, objectMapper, emailContactService, emailSubscriptionTracker, proxyPrimaryUrl
        )
    }

    @Test
    fun `test that every template has test data`() {
        TypedEmailData::class.sealedSubclasses.forEach { subclass ->
            assertTrue(typedEmailTestData.any { subclass.isInstance(it) })
        }
    }

    @Test
    fun `test that every template can be constructed without exception`() {
        val correlationId = "correlationId"
        val receiver = listOf(recipientToContactMap.keys.first())
        val cc = emptyList<EmailRecipient>()
        val bcc = emptyList<EmailRecipient>()

        doNothing().whenever(emailSender).sendEmail(any())

        typedEmailTestData.forEach { testData ->
            val jsonString = objectMapper.writeValueAsString(EmailMessage(testData, receiver, cc, bcc))
            assertDoesNotThrow {
                emailMessageListener.sendEmail(jsonString, MessageType.SEND_EMAIL, correlationId)
            }
        }
    }
}