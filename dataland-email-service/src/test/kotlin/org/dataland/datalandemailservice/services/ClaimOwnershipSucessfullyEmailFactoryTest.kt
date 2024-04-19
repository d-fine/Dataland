package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.services.templateemail.ClaimOwnershipSucessfullyEmailFactory
import org.dataland.datalandemailservice.services.templateemail.DataRequestedClaimOwnershipEmailFactory
import org.dataland.datalandemailservice.utils.assertEmailContactInformationEquals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClaimOwnershipSucessfullyEmailFactoryTest {
    private val proxyPrimaryUrl = "local-dev.dataland.com"
    private val senderEmail = "sender@example.com"
    private val senderName = "Test"
    private val companyName = "Test Inc."
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val requesterEmail = "requester@bigplayer.com"
    private val receiverEmail = "testReceiver@somewhere.com"

    private fun buildTestEmail(): Email {
        val properties = mapOf(
            "companyId" to companyId,
            "companyName" to companyName,
            "requesterEmail" to requesterEmail
        )

        val email = ClaimOwnershipSucessfullyEmailFactory(
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
        val email = buildTestEmail()

        assertEmailContactInformationEquals(
            EmailContact(senderEmail, senderName),
            setOf(EmailContact(receiverEmail)),
            emptySet(),
            email,
        )
        assertTrue(email.content.htmlContent.contains("DATALAND"))
        assertTrue(email.content.htmlContent.contains("Great news!"))
        assertTrue(email.content.htmlContent.contains("You've successfully claimed data " +
                "ownership for"))
        assertTrue(email.content.htmlContent.contains("Now, take the next step to access your " +
                "company overview, view your data requests, and provide data."))
        assertTrue(email.content.htmlContent.contains("Copyright"))
        assertTrue(email.content.htmlContent.contains(companyName))
        assertTrue(
            email.content.htmlContent.contains(
                "href=\"https://$proxyPrimaryUrl/companies/$companyId\"",
            ),
        )
    }
}
