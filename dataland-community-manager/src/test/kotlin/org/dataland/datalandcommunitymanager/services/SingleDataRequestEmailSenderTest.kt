package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandemail.email.Email
import org.dataland.datalandemail.email.EmailContact
import org.dataland.datalandemail.email.EmailSender
import org.dataland.datalandemail.utils.EmailMatchingPattern
import org.dataland.datalandemail.utils.assertEmailMatchesPattern
import org.dataland.datalandemail.utils.toEmailContacts
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class SingleDataRequestEmailSenderTest {
    private lateinit var singleDataRequestEmailSender: SingleDataRequestEmailSender
    private lateinit var mockEmailSender: EmailSender

    private val mockRequesterAuthentication = AuthenticationMock.mockJwtAuthentication(
        "requester@test.com",
        "user-id",
        emptySet(),
    )

    private val proxyPrimaryUrl = "https://local-dev.dataland.com"

    private val dataType = DataTypeEnum.lksg

    private val properCompanyId = "d623c5b6-ba18-23c3-1234-333555554444"
    private val companyIdentifier = "DEsomething"
    private val companyName = "Real Company"
    private val defaultMessage = "Dummy Message"

    private val reportingPeriods = listOf("2023", "2022")

    private val senderEmail = "info@dataland.com"
    private val senderName = "Dataland"

    private val semicolonSeparatedInternalReceiverEmails = "testReceiver@dataland.com"
    private val semicolonSeparatedInternalCcEmails = "testCc@dataland.com"
    private val internalReceivers = semicolonSeparatedEmailsToEmailContacts(semicolonSeparatedInternalReceiverEmails)
    private val internalCc = semicolonSeparatedEmailsToEmailContacts(semicolonSeparatedInternalCcEmails)

    @BeforeEach
    fun setupSingleDataRequestEmailSender() {
        val mockCompanyGetter: CompanyGetter = mock(CompanyGetter::class.java)
        val singleDataRequestEmailBuilder = SingleDataRequestEmailBuilder(
            proxyPrimaryUrl,
            senderEmail,
            senderName,
            mockCompanyGetter,
        )
        val singleDataRequestInternalEmailBuilder = SingleDataRequestInternalEmailBuilder(
            proxyPrimaryUrl,
            senderEmail,
            senderName,
            semicolonSeparatedInternalReceiverEmails,
            semicolonSeparatedInternalCcEmails,
            mockCompanyGetter,
        )
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
                properCompanyId,
                dataType,
                reportingPeriods = listOf(),
                contacts = listOf("receiver@abc.de", "otherreceiver@something.else"),
                message = defaultMessage,
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
                properCompanyId,
                dataType,
                reportingPeriods = reportingPeriods,
                contacts = listOf("receiver@abc.de", "otherreceiver@something.else"),
                message = defaultMessage,
            ),
            DataRequestCompanyIdentifierType.Isin,
            companyIdentifier,
        )
        assertNumEmailsSentEquals(1)
    }

    @Test
    fun `validate that an internal email is sent if there are no contacts provided`() {
        expectSentEmailsToMatchInternalEmail(
            DataRequestCompanyIdentifierType.DatalandCompanyId,
            true,
        )
        singleDataRequestEmailSender.sendSingleDataRequestEmails(
            mockRequesterAuthentication,
            SingleDataRequest(
                properCompanyId,
                dataType,
                reportingPeriods = reportingPeriods,
                contacts = listOf(),
                message = defaultMessage,
            ),
            DataRequestCompanyIdentifierType.DatalandCompanyId,
            companyIdentifier,
        )
        assertNumEmailsSentEquals(1)
    }

    @Test
    fun `validate that multiple external emails are sent to the provided contacts for a Dataland company ID`() =
        assertContactEmailsAreSent(listOf("contact@provider.com", "othercontact@provider.com")) { contactEmails ->
            singleDataRequestEmailSender.sendSingleDataRequestEmails(
                mockRequesterAuthentication,
                SingleDataRequest(
                    properCompanyId,
                    dataType,
                    reportingPeriods = reportingPeriods,
                    contacts = contactEmails,
                    message = defaultMessage,
                ),
                DataRequestCompanyIdentifierType.DatalandCompanyId,
                companyIdentifier,
            )
        }

    private fun expectSentEmailsToMatch(
        matchingPattern: EmailMatchingPattern,
    ) {
        val mockEmail = mock(Email::class.java)
        `when`(mockEmailSender.sendEmail(any() ?: mockEmail)).then { invocation ->
            val emailToSend = invocation.arguments[0] as Email
            assertEmailMatchesPattern(emailToSend, matchingPattern)
        }
    }

    private val baseInternalEmailMatchingPattern = EmailMatchingPattern(
        expectedSender = EmailContact(senderEmail, senderName),
        expectedReceiversGetter = { internalReceivers },
        expectedCc = internalCc,
        expectedSubject = "Dataland Single Data Request",
        expectedToBeContainedInTextContent = emptySet(),
        expectedToBeContainedInHtmlContent = emptySet(),
        expectedNotToBeContainedInTextContent = emptySet(),
        expectedNotToBeContainedInHtmlContent = emptySet(),
    )

    private fun expectSentEmailsToMatchInternalEmail(
        companyIdentifierType: DataRequestCompanyIdentifierType,
        companyNameExpected: Boolean = false,
    ) {
        val properties = mutableMapOf(
            "Environment" to proxyPrimaryUrl,
            "User" to "User ${mockRequesterAuthentication.username}" +
                " (Keycloak ID: ${mockRequesterAuthentication.userId})",
            "Data Type" to dataType.name,
            "Reporting Periods" to reportingPeriods.joinToString(", "),
            "Company Identifier (${companyIdentifierType.name})" to companyIdentifier,
        )
        val unexpectedValues = if (companyNameExpected) {
            properties["Company Name"] = companyName
            emptySet()
        } else {
            setOf(companyName)
        }
        expectSentEmailsToMatch(
            baseInternalEmailMatchingPattern.copy(
                expectedToBeContainedInTextContent = properties.map { "${it.key}: ${it.value}" }.toSet(),
                expectedToBeContainedInHtmlContent = properties.map { "${it.key}: </span> ${it.value}" }.toSet(),
                expectedNotToBeContainedInTextContent = unexpectedValues,
                expectedNotToBeContainedInHtmlContent = unexpectedValues,
            ),
        )
    }

    private val baseContactEmailMatchingPattern = EmailMatchingPattern(
        expectedSender = EmailContact(senderEmail, senderName),
        expectedReceiversGetter = { setOf(EmailContact("placeholder")) },
        expectedCc = emptySet(),
        expectedSubject = "A message from Dataland: Your ESG data are high on demand!",
        expectedToBeContainedInTextContent = emptySet(),
        expectedToBeContainedInHtmlContent = emptySet(),
        expectedNotToBeContainedInTextContent = emptySet(),
        expectedNotToBeContainedInHtmlContent = emptySet(),
    )

    fun expectSentEmailsToMatchContactEmail(expectedReceiversGetter: () -> Set<EmailContact>) {
        val sharedContent = setOf(
            "from $companyName",
            "$proxyPrimaryUrl/companies/$companyIdentifier",
            "LkSG",
            reportingPeriods.joinToString(", "),
            mockRequesterAuthentication.username,
        )
        expectSentEmailsToMatch(
            baseContactEmailMatchingPattern.copy(
                expectedReceiversGetter = expectedReceiversGetter,
                expectedToBeContainedInTextContent = sharedContent,
                expectedToBeContainedInHtmlContent = sharedContent,
            ),
        )
    }

    private fun assertContactEmailsAreSent(contactEmails: List<String>, test: (List<String>) -> Unit) {
        val unaddressedContactEmails = contactEmails.toMutableList()
        expectSentEmailsToMatchContactEmail { setOf(EmailContact(unaddressedContactEmails.removeFirst())) }
        test(contactEmails)
        assertEquals(0, unaddressedContactEmails.size)
        assertNumEmailsSentEquals(contactEmails.size)
    }

    private fun assertNumEmailsSentEquals(expectedNumberOfEmailsSent: Int = 1) {
        val mockEmail = mock(Email::class.java)
        verify(mockEmailSender, times(expectedNumberOfEmailsSent)).sendEmail(any() ?: mockEmail)
    }

    private fun semicolonSeparatedEmailsToEmailContacts(semicolonSeparatedEmails: String): Set<EmailContact> =
        semicolonSeparatedEmails.split(";").filter { it.isNotBlank() }.toEmailContacts()
}
