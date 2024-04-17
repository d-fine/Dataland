package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.services.templateemail.DataRequestedClaimOwnershipEmailFactory
import org.dataland.datalandemailservice.utils.assertEmailContactInformationEquals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ClaimOwnershipSucessfullyEmailFactoryTest {
    private val proxyPrimaryUrl = "local-dev.dataland.com"
    private val senderEmail = "sender@example.com"
    private val senderName = "Test"
    private val companyName = "Test Inc."
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val requesterEmail = "requester@bigplayer.com"
    private val dataType = "LkSG"
    private val contactMessage = "something"
    private val receiverEmail = "testReceiver@somewhere.com"

    private fun buildTestEmail(): Email {
        val properties = mapOf(
            "companyId" to companyId,
            "companyName" to companyName,
            "requesterEmail" to requesterEmail,
            "dataType" to dataType,
            "message" to contactMessage,
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
        val email = buildTestEmail()

        assertEmailContactInformationEquals(
            EmailContact(senderEmail, senderName),
            setOf(EmailContact(receiverEmail)),
            emptySet(),
            email,
        )
        Assertions.assertTrue(email.content.htmlContent.contains(requesterEmail))
        Assertions.assertTrue(email.content.htmlContent.contains(companyName))
        Assertions.assertTrue(email.content.htmlContent.contains(dataType))
        Assertions.assertTrue(
            email.content.htmlContent.contains(
                "href=\"https://$proxyPrimaryUrl/companies/$companyId\"",
            ),
        )
    }
}
