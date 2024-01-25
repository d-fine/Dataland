package org.dataland.datalandbackend.email

import org.dataland.datalandbackend.services.DataOwnershipRequestEmailBuilder
import org.dataland.datalandbackendutils.email.BaseEmailBuilder
import org.dataland.datalandbackendutils.email.Email
import org.dataland.datalandbackendutils.email.EmailContact
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class DataOwnershipRequestEmailBuilderTest {
    @Test
    fun `validate that the output of the base email builder is correctly formatted`() {
        val senderEmail = "sender@dataland.com"
        val senderName = "Test"
        val receiverEmails = listOf(
            "receiver1@dataland.com",
            "receiver2@dataland.com",
        )
        val ccEmails = listOf(
            "cc1@dataland.com",
        )
        val environment = "test.dataland.com"
        val mockAuthentication = mock(DatalandJwtAuthentication::class.java)
        val companyId = "8"
        val comment = "This is a comment"
        val userId = "user-id"
        val username = "i.love@dataland.com"
        `when`(mockAuthentication.userId).thenReturn(userId)
        `when`(mockAuthentication.username).thenReturn(username)
        val email = DataOwnershipRequestEmailBuilder(
            proxyPrimaryUrl = environment,
            senderEmail = senderEmail,
            senderName = senderName,
            semicolonSeparatedReceiverEmails = receiverEmails.joinToString(";"),
            semicolonSeparatedCcEmails = ccEmails.joinToString(";"),
        ).buildDataOwnershipRequest(
            companyId,
            mockAuthentication,
            comment
        )

        assertEquals(EmailContact(senderEmail, senderName), email.sender)
        assertEquals(receiverEmails.size, email.receivers.size)
        receiverEmails.forEachIndexed { index, it ->
            assertEquals(EmailContact(it), email.receivers[index])
        }
        assertEquals(ccEmails.size, email.cc!!.size)
        ccEmails.forEachIndexed { index, it ->
            assertEquals(EmailContact(it), email.cc!![index])
        }
        mapOf(
            "Environment" to environment,
            "User" to "User $username (Keycloak ID: $userId)",
            "Company (Dataland ID)" to companyId,
            "Comment" to comment,
        ).forEach {
            assertTrue(email.content.textContent.contains("${it.key}: ${it.value}"))
        }
    }
}