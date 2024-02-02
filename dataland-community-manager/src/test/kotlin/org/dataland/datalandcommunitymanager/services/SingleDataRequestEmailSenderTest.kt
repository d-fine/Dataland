package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.DatalandCommunityManager
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandemail.email.Email
import org.dataland.datalandemail.email.EmailContact
import org.dataland.datalandemail.email.EmailSender
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest(classes = [DatalandCommunityManager::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class SingleDataRequestEmailSenderTest(
    @Value("\${dataland.proxy.primary.url}") private val proxyPrimaryUrl: String,
    @Value("\${dataland.notification.data-request.internal.receivers}")
    semicolonSeparatedInternalReceiverEmails: String,
    @Value("\${dataland.notification.data-request.internal.cc}") semicolonSeparatedInternalCcEmails: String,
) {
    private lateinit var singleDataRequestEmailSender: SingleDataRequestEmailSender
    private lateinit var mockEmailSender: EmailSender

    private val mockRequesterAuthentication = AuthenticationMock.mockJwtAuthentication(
        "requester@test.com",
        "user-id",
        emptySet(),
    )

    private var contactEmails: MutableList<String> = mutableListOf()

    private val dataType = DataTypeEnum.lksg

    private val companyIdentifier = "DEsomething"
    private val companyName = "Real Company"

    private val reportingPeriods = listOf("2023", "2022")

    private val internalReceivers = semicolonSeparatedEmailsToEmailContacts(semicolonSeparatedInternalReceiverEmails)
    private val internalCc = semicolonSeparatedEmailsToEmailContacts(semicolonSeparatedInternalCcEmails)

    @MockBean
    lateinit var mockCompanyGetter: CompanyGetter

    @Autowired
    lateinit var singleDataRequestInternalEmailBuilder: SingleDataRequestInternalEmailBuilder

    @Autowired
    lateinit var singleDataRequestEmailBuilder: SingleDataRequestEmailBuilder

    @BeforeEach
    fun setupSingleDataRequestEmailSender() {
        mockEmailSender = mock(EmailSender::class.java)
        singleDataRequestEmailSender = SingleDataRequestEmailSender(
            mockEmailSender,
            singleDataRequestEmailBuilder,
            singleDataRequestInternalEmailBuilder,
        )
        val mockCompanyInformation = mock(CompanyInformation::class.java)
        `when`(mockCompanyInformation.companyName).thenReturn(companyName)
        `when`(mockCompanyGetter.getCompanyInfo(companyIdentifier)).thenReturn(mockCompanyInformation)
    }

    @Test
    fun `validate that no email is sent if there are no reporting periods provided`() {
        singleDataRequestEmailSender.sendSingleDataRequestEmails(
            mockRequesterAuthentication,
            SingleDataRequest(
                "unused",
                dataType,
                listOfReportingPeriods = listOf(),
                contactList = listOf("receiver@abc.de", "otherreceiver@something.else"),
                message = "not of interest",
            ),
            DataRequestCompanyIdentifierType.Isin,
            companyIdentifier,
        )
        assertNumEmailsSentEquals(0)
    }

    @Test
    fun `validate that an internal email is sent if there is no Dataland company ID provided`() {
        expectSentEmailsToMatchInternalEmail(
            DataRequestCompanyIdentifierType.Isin,
        )
        singleDataRequestEmailSender.sendSingleDataRequestEmails(
            mockRequesterAuthentication,
            SingleDataRequest(
                "unused",
                dataType,
                listOfReportingPeriods = reportingPeriods,
                contactList = listOf("receiver@abc.de", "otherreceiver@something.else"),
                message = "not of interest",
            ),
            DataRequestCompanyIdentifierType.Isin,
            companyIdentifier,
        )
        assertNumEmailsSentEquals(1)
    }

    @Test
    fun `validate that an internal email is sent if there are no contact provided`() {
        expectSentEmailsToMatchInternalEmail(
            DataRequestCompanyIdentifierType.DatalandCompanyId,
            true,
        )
        singleDataRequestEmailSender.sendSingleDataRequestEmails(
            mockRequesterAuthentication,
            SingleDataRequest(
                "unused",
                dataType,
                listOfReportingPeriods = reportingPeriods,
                contactList = listOf(),
                message = "not of interest",
            ),
            DataRequestCompanyIdentifierType.DatalandCompanyId,
            companyIdentifier,
        )
        assertNumEmailsSentEquals(1)
    }

    @Test
    fun `validate that an external email is sent to the provided contact for a Dataland company ID`() {
        contactEmails = mutableListOf("contact@provider.com", "othercontact@provider.com")
        expectSentEmailsToMatchContactEmail()
        println(contactEmails.size)
        singleDataRequestEmailSender.sendSingleDataRequestEmails(
            mockRequesterAuthentication,
            SingleDataRequest(
                "unused",
                dataType,
                listOfReportingPeriods = reportingPeriods,
                contactList = contactEmails.toList(),
//                contactList = listOf("contact@provider.com", "othercontact@provider.com"), TODO test multiple contacts
                message = "not of interest",
            ),
            DataRequestCompanyIdentifierType.DatalandCompanyId,
            companyIdentifier,
        )
        assertEquals(0, contactEmails.size)
        assertNumEmailsSentEquals(2)
    }

    private fun expectSentEmailsToMatch(
        expectedSender: EmailContact,
        expectedReceiversGetter: () -> List<EmailContact>,
        expectedCc: List<EmailContact>,
        expectedSubject: String,
        expectedToBeContainedInTextContent: List<String>,
        expectedToBeContainedInHtmlContent: List<String>,
        expectedNotToBeContainedInTextContent: List<String> = emptyList(),
        expectedNotToBeContainedInHtmlContent: List<String> = emptyList(),
    ) {
        val mockEmail = mock(Email::class.java)
        `when`(mockEmailSender.sendEmail(any() ?: mockEmail)).then { invocation ->
            val emailToSend = invocation.arguments[0] as Email
            assertEquals(expectedSender, emailToSend.sender)
            assertEquals(expectedReceiversGetter(), emailToSend.receivers)
            assertEquals(expectedCc, emailToSend.cc ?: emptyList<EmailContact>())
            assertEquals(expectedSubject, emailToSend.content.subject)
            expectedToBeContainedInTextContent.forEach {
                assertTrue(emailToSend.content.textContent.contains(it))
            }
            expectedToBeContainedInHtmlContent.forEach {
                assertTrue(emailToSend.content.htmlContent.contains(it))
            }
            expectedNotToBeContainedInTextContent.forEach {
                assertFalse(emailToSend.content.textContent.contains(it))
            }
            expectedNotToBeContainedInHtmlContent.forEach {
                assertFalse(emailToSend.content.htmlContent.contains(it))
            }
        }
    }

    private fun expectSentEmailsToMatchInternalEmail(
        companyIdentifierType: DataRequestCompanyIdentifierType,
        companyNameExpected: Boolean = false,
    ) {
        val properties = mutableMapOf(
            "User" to "User ${mockRequesterAuthentication.username}" +
                " (Keycloak ID: ${mockRequesterAuthentication.userId})",
            "Data Type" to dataType.name,
            "Reporting Periods" to reportingPeriods.joinToString(", "),
            "Company Identifier (${companyIdentifierType.name})" to companyIdentifier,
        )
        val unexpectedValues = if (companyNameExpected) {
            properties["Company Name"] = companyName
            emptyList()
        } else {
            listOf(companyName)
        }
        expectSentEmailsToMatch(
            expectedSender = EmailContact("info@dataland.com", "Dataland"),
            expectedReceiversGetter = { internalReceivers },
            expectedCc = internalCc,
            expectedSubject = "Dataland Single Data Request",
            expectedToBeContainedInTextContent = properties.map { "${it.key}: ${it.value}" },
            expectedToBeContainedInHtmlContent = properties.map { "${it.key}: </span> ${it.value}" },
            expectedNotToBeContainedInTextContent = unexpectedValues,
            expectedNotToBeContainedInHtmlContent = unexpectedValues,
        )
    }

    fun expectSentEmailsToMatchContactEmail() {
        val sharedContent = listOf(
            "from $companyName",
            "$proxyPrimaryUrl/companies/$companyIdentifier",
            "LkSG",
            reportingPeriods.joinToString(", "),
            mockRequesterAuthentication.username,
        )
        expectSentEmailsToMatch(
            expectedSender = EmailContact("info@dataland.com", "Dataland"),
            expectedReceiversGetter = { listOf(EmailContact(contactEmails.removeFirst())) },
            expectedCc = listOf(),
            expectedSubject = "A message from Dataland: Your ESG data are high on demand!",
            expectedToBeContainedInTextContent = sharedContent,
            expectedToBeContainedInHtmlContent = sharedContent,
        )
    }

    private fun assertNumEmailsSentEquals(expectedNumberOfEmailsSent: Int = 1) {
        val mockEmail = mock(Email::class.java)
        verify(mockEmailSender, times(expectedNumberOfEmailsSent)).sendEmail(any() ?: mockEmail)
    }

    private fun semicolonSeparatedEmailsToEmailContacts(semicolonSeparatedEmails: String): List<EmailContact> =
        semicolonSeparatedEmails.split(";").filter { it.isNotBlank() }.map { EmailContact(it) }
}
