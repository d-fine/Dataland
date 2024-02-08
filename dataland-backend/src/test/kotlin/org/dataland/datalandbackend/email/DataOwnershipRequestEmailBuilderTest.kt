package org.dataland.datalandbackend.email

import org.dataland.datalandbackend.services.DataOwnershipRequestEmailBuilder
import org.dataland.datalandemail.email.EmailContact
import org.dataland.datalandemail.utils.assertEmailContactInformationEquals
import org.dataland.datalandemail.utils.toEmailContacts
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class DataOwnershipRequestEmailBuilderTest {

    private val senderEmail = "sender@dataland.com"
    private val senderName = "Test"
    private val receiverEmails = listOf("receiver1@dataland.com", "receiver2@dataland.com")
    private val ccEmails = listOf("cc1@dataland.com")
    private val environment = "test.dataland.com"
    private val companyId = "8"
    private val companyName = "Test Inc."
    private val comment = "This is a comment"

    @Test
    fun `validate that the output of the data ownership request email builder is correctly formatted`() {
        val mockAuthentication = mock(DatalandJwtAuthentication::class.java)
        `when`(mockAuthentication.userId).thenReturn("user-id")
        `when`(mockAuthentication.username).thenReturn("i.love@dataland.com")
        val email = DataOwnershipRequestEmailBuilder(
            proxyPrimaryUrl = environment,
            senderEmail = senderEmail,
            senderName = senderName,
            semicolonSeparatedReceiverEmails = receiverEmails.joinToString(";"),
            semicolonSeparatedCcEmails = ccEmails.joinToString(";"),
        ).buildDataOwnershipRequest(companyId, companyName, mockAuthentication, comment)
        assertEmailContactInformationEquals(
            EmailContact(senderEmail, senderName),
            receiverEmails.toEmailContacts(),
            ccEmails.toEmailContacts(),
            email,
        )
        mapOf(
            "Environment" to environment,
            "User" to "User ${mockAuthentication.username} (Keycloak ID: ${mockAuthentication.userId})",
            "Company (Dataland ID)" to companyId,
            "Company Name" to companyName,
            "Comment" to comment,
        ).forEach {
            assertTrue(email.content.textContent.contains("${it.key}: ${it.value}"))
        }
    }
}
