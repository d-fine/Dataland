package org.dataland.datalandemailservice.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.email.EmailSender
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetGranted
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetRequested
import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApproved
import org.dataland.datalandmessagequeueutils.messages.email.DatasetRequestedClaimOwnership
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.MultipleDatasetsUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.SingleDatasetUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailData
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.*

class EmailMessageListenerTest {

    private lateinit var emailSender: EmailSender
    private var objectMapper  = jacksonObjectMapper()
    private lateinit var emailContactService: EmailContactService
    private lateinit var emailSubscriptionTracker: EmailSubscriptionTracker
    private val proxyPrimaryUrl = "abc.example.com"

    private lateinit var emailMessageListener: EmailMessageListener

    private val testDatasetRequestedClaimOwnership = DatasetRequestedClaimOwnership(
        "companyId", "companyName", "requesterEmail", "dataType", listOf("2020", "2023"),
        "message", "firstName", "lastName"
    )

    private val testCompanyOwnershipClaimApproved = CompanyOwnershipClaimApproved(
        "companyId", "companyName", 10
    )

    private val testAccessToDatasetRequested = AccessToDatasetRequested(
        "companyId", "companyName", "dataType", listOf("2020", "2023"),
        "message", "requesterEmail", "requesterFirstName", "requesterLastName"
    )

    private val testSingleDatasetUploadedEngagement = SingleDatasetUploadedEngagement(
        "companyId", "companyName", "dataType", "2023"
    )

    private val testMultipleDatasetsUploadedEngagement = MultipleDatasetsUploadedEngagement(
        "companyId", "companyName",
        listOf(
            MultipleDatasetsUploadedEngagement.FrameworkData("dataTypeA", listOf("2020", "2021")),
            MultipleDatasetsUploadedEngagement.FrameworkData("dataTypeB", listOf("2023"))
            ),
        3,
    )

    private val testAccessToDatasetGranted = AccessToDatasetGranted(
        "companyId", "companyName", "dataType", "dataTypeDescription",
        "reportingPeriod", "creationDate"
    )

    private val typedEmailTestData: List<TypedEmailData> = listOf(
        testDatasetRequestedClaimOwnership, testAccessToDatasetRequested, testSingleDatasetUploadedEngagement, testMultipleDatasetsUploadedEngagement,
        testAccessToDatasetGranted, testCompanyOwnershipClaimApproved
    )

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
    fun `test that every template has test data`() {
        TypedEmailData::class.sealedSubclasses.forEach { subclass ->
            assertTrue(typedEmailTestData.any { subclass.isInstance(it) })
        }
    }

    @Test
    fun `test that every template can be constructed without exception`() {
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

    @Test
    fun `test that blocked contacts are no receiver`() {
        val receiver = recipientToContactMap.keys.toList()
        val cc = recipientToContactMap.keys.toList()
        val bcc = recipientToContactMap.keys.toList()

        val allowed = listOf(EmailContact("1@example.com"), EmailContact("3@example.com"))

        val jsonString = objectMapper.writeValueAsString(EmailMessage(testAccessToDatasetRequested, receiver, cc, bcc))

        doNothing().whenever(emailSender).sendEmail(any())

        emailMessageListener.sendEmail(jsonString, MessageType.SEND_EMAIL, correlationId)

        verify(emailSender).sendEmail(argThat { email ->
            email.receivers == allowed && email.cc == allowed && email.bcc == allowed
        })
    }
}