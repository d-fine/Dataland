package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.services.templateemail.DataRequestedClaimOwnershipEmailFactory
import org.dataland.datalandemailservice.utils.assertEmailContactInformationEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DataRequestedClaimOwnershipEmailFactoryTest {
    private val proxyPrimaryUrl = "local-dev.dataland.com"
    private val senderEmail = "sender@dataland.com"
    private val senderName = "Test"
    private val companyName = "Test Inc."
    private val reportingPeriods = "2022, 2023"
    private val reportingPeriod = "2022"
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val requesterEmail = "requester@bigplayer.com"
    private val dataType = "LkSG"
    private val contactMessage = "something"
    private val receiverEmail = "testReceiver@somewhere.com"

    private fun buildTestEmail(
        multiplePeriods: Boolean,
    ): Email {
        val properties = mapOf(
            "companyId" to companyId,
            "companyName" to companyName,
            "requesterEmail" to requesterEmail,
            "dataType" to dataType,
            "reportingPeriods" to if (multiplePeriods) reportingPeriods else reportingPeriod,
            "message" to contactMessage.takeIf { !contactMessage.isNullOrBlank() },
        )

        val email = DataRequestedClaimOwnershipEmailFactory(
            proxyPrimaryUrl = proxyPrimaryUrl,
            senderEmail = senderEmail,
            senderName = senderName,
        ).buildEmail(
            receiverEmail = receiverEmail,
            properties = properties,
        )

        return email
    }

    @Test
    fun `validate that the output of the claim ownership mail is correctly formatted`() {
        val email = buildTestEmail(true)

        assertEmailContactInformationEquals(
            EmailContact(senderEmail, senderName),
            setOf(EmailContact(receiverEmail)),
            emptySet(),
            email,
        )
        assertTrue(email.content.htmlContent.contains(requesterEmail))
        assertTrue(email.content.htmlContent.contains(companyName))
        assertTrue(email.content.htmlContent.contains(dataType))
        assertTrue(email.content.htmlContent.contains(reportingPeriods))
        assertTrue(
            email.content.htmlContent.contains(
                "href=\"https://$proxyPrimaryUrl/companies/$companyId\"",
            ),
        )
    }

    @Test
    fun `validate that the text content of the claim ownership mail is correctly formatted with multiple periods`() {
        val email = buildTestEmail(true)
        val hasMultipleReportingPeriods = "s"
        assertTrue(
            email.content.textContent.contains(
                "Greetings!\n\nYou have been invited to provide data on Dataland.\n",
            ),
        )
        assertTrue(
            email.content.textContent.contains(
                " from $companyName for the year$hasMultipleReportingPeriods",
            ),
        )
        assertTrue(email.content.textContent.contains(" $reportingPeriods.\n"))
        assertTrue(email.content.textContent.contains("User $requesterEmail sent the following message:\n"))
        assertTrue(email.content.textContent.contains(contactMessage))
        assertTrue(
            email.content.textContent.contains(
                "\n\nRegister as a data owner on $proxyPrimaryUrl/companies/$companyId",
            ),
        )
    }

    @Test
    fun `validate that the text content of the claim ownership mail is correctly formatted with single period`() {
        val email = buildTestEmail(false)
        val hasMultipleReportingPeriods = ""
        assertTrue(
            email.content.textContent.contains(
                "Greetings!\n\nYou have been invited to provide data on Dataland.\n",
            ),
        )
        assertTrue(
            email.content.textContent.contains(
                " from $companyName for the year$hasMultipleReportingPeriods",
            ),
        )
        assertTrue(email.content.textContent.contains(" $reportingPeriod.\n"))
        assertTrue(email.content.textContent.contains("User $requesterEmail sent the following message:\n"))
        assertTrue(email.content.textContent.contains(contactMessage))
        assertTrue(
            email.content.textContent.contains(
                "\n\nRegister as a data owner on $proxyPrimaryUrl/companies/$companyId",
            ),
        )
    }
}
