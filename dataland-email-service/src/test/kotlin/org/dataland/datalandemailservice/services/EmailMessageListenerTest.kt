package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandemailservice.email.TypedEmailContentTestData
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetGranted
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetRequested
import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApproved
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestAnswered
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestClosed
import org.dataland.datalandmessagequeueutils.messages.email.DatasetRequestedClaimOwnership
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.KeyValueTable
import org.dataland.datalandmessagequeueutils.messages.email.MultipleDatasetsUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.SingleDatasetUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.*

class EmailMessageListenerTest {

    private lateinit var emailSender: EmailSender
    private var objectMapper  = jacksonObjectMapper()
    private lateinit var emailContactService: EmailContactService
    private lateinit var emailSubscriptionTracker: EmailSubscriptionTracker
    private val proxyPrimaryUrl = "abc.example.com"

    private lateinit var emailMessageListener: EmailMessageListener

    private val recipientToContactMap = mapOf(
        EmailRecipient.EmailAddress("1@example.com") to EmailContact("1@example.com"),
        EmailRecipient.Internal to EmailContact("2@example.com"),
        EmailRecipient.EmailAddress("3@example.com") to EmailContact("3@example.com"),
        EmailRecipient.UserId("User-a") to EmailContact("a@example.com"),
    )

    private val senderContact = EmailContact("sender@example.com")

    private val contactToSubscriptionStatusMap = mapOf(
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
                blocked
            )
        }

        emailMessageListener = EmailMessageListener(
            emailSender, objectMapper, emailContactService, emailSubscriptionTracker, proxyPrimaryUrl
        )
    }

    @Test
    fun `test that blocked contacts are no receiver`() {
        // TODO brauchen wir den test noch, bzw den ganzen aufbau von den mocks, was kann man noch testen?
        val receiver = recipientToContactMap.keys.toList()
        val cc = recipientToContactMap.keys.toList()
        val bcc = recipientToContactMap.keys.toList()

        val allowed = listOf(EmailContact("1@example.com"), EmailContact("3@example.com"))

        val typedEmailContent = TypedEmailContentTestData.accessToDatasetRequested
        val jsonString = objectMapper.writeValueAsString(EmailMessage(typedEmailContent, receiver, cc, bcc))

        doNothing().whenever(emailSender).sendEmail(any())

        emailMessageListener.handleSendEmailMessage(jsonString, MessageType.SEND_EMAIL, correlationId)

        verify(emailSender).sendEmail(argThat { email ->
            email.receivers == allowed && email.cc == allowed && email.bcc == allowed
        })
    }
}