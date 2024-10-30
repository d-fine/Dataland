package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.services.templateemail.DataRequestedClaimOwnershipEmailFactory
import org.dataland.datalandemailservice.utils.assertEmailContactInformationEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import java.util.UUID

class DataRequestedClaimOwnershipEmailFactoryTest {
    private val proxyPrimaryUrl = "local-dev.dataland.com"
    private val senderEmail = "sender@example.com"
    private val senderName = "Test"
    private val companyName = "Test Inc."
    private val reportingPeriods = "2022, 2023"
    private val reportingPeriod = "2022"
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val requesterEmail = "requester@bigplayer.com"
    private val dataType = "LkSG"
    private val contactMessage = "something"
    private val receiverEmail = "testReceiver@somewhere.com"
    private val dummyFirstName = "testUserName"
    private val dummyLastName = "testUserLastName"

    private var subscriptionUuid = UUID.randomUUID()

    private lateinit var emailSubscriptionService: EmailSubscriptionService
    private lateinit var dataRequestedClaimOwnershipEmailFactory: DataRequestedClaimOwnershipEmailFactory

    @BeforeEach
    fun setup() {
        emailSubscriptionService = mock(EmailSubscriptionService::class.java)
        `when`(emailSubscriptionService.insertSubscriptionEntityIfNeededAndReturnUuid(any())).thenReturn(subscriptionUuid)
        dataRequestedClaimOwnershipEmailFactory =
            DataRequestedClaimOwnershipEmailFactory(
                proxyPrimaryUrl = proxyPrimaryUrl,
                senderEmail = senderEmail,
                senderName = senderName,
                emailSubscriptionService = emailSubscriptionService,
            )
    }

    private fun buildTestEmail(reportingPeriods: String): Email {
        val properties =
            mapOf(
                "companyId" to companyId,
                "companyName" to companyName,
                "requesterEmail" to requesterEmail,
                "dataType" to dataType,
                "reportingPeriods" to reportingPeriods,
                "message" to contactMessage,
                "firstName" to dummyFirstName,
                "lastName" to dummyLastName,
            )
        return dataRequestedClaimOwnershipEmailFactory.buildEmail(receiverEmail, properties)
    }

    @Test
    fun `validate that the output of the claim ownership mail is correctly formatted`() {
        val email = buildTestEmail(reportingPeriods)

        assertEmailContactInformationEquals(
            EmailContact(senderEmail, senderName),
            setOf(EmailContact(receiverEmail)),
            emptySet(),
            email,
        )
        assertTrue(email.content.htmlContent.contains(requesterEmail))
        assertTrue(email.content.htmlContent.contains(companyName))
        assertTrue(email.content.htmlContent.contains(dataType))
        assertTrue(email.content.htmlContent.contains(dummyFirstName))
        assertTrue(email.content.htmlContent.contains(dummyLastName))
        assertTrue(email.content.htmlContent.contains(reportingPeriods))
        assertTrue(
            email.content.htmlContent.contains(
                "href=\"https://$proxyPrimaryUrl/companies/$companyId\"",
            ),
        )
        assertTrue(email.content.htmlContent.contains("/unsubscribe/$subscriptionUuid"))
    }

    @Test
    fun `validate that the text content of the claim ownership mail is correctly formatted with multiple periods`() {
        validateTextContent(reportingPeriods)
    }

    @Test
    fun `validate that the text content of the claim ownership mail is correctly formatted with single period`() {
        validateTextContent(reportingPeriod)
    }

    private fun validateTextContent(reportingPeriods: String) {
        val email = buildTestEmail(reportingPeriods)
        val pluralSuffix = if (reportingPeriods.contains(",")) "s" else ""
        assertTrue(
            email.content.textContent.contains(
                "Greetings!\n\nYou have been invited to provide data on Dataland.\n",
            ),
        )
        assertTrue(
            email.content.textContent.contains(
                " from $companyName for the year$pluralSuffix",
            ),
        )
        assertTrue(email.content.textContent.contains(" $reportingPeriods.\n"))
        assertTrue(email.content.textContent.contains("User $requesterEmail sent the following message:\n"))
        assertTrue(email.content.textContent.contains(contactMessage))
        assertTrue(
            email.content.textContent.contains(
                "\n\nRegister as a company owner on $proxyPrimaryUrl/companies/$companyId",
            ),
        )
    }
}
