package org.dataland.datalandemailservice.services

import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class EmailContactServiceTest {
    private lateinit var keycloakUserService: KeycloakUserService

    private val internalRecipients = "internal1@example.com;internal2@example.com"
    private val internalEmailAddresses = listOf("internal1@example.com", "internal2@example.com")

    private val internalCcRecipients = "internalcc1@example.com;internalcc2@example.com"
    private val internalCcEmailAddresses = listOf("internalcc1@example.com", "internalcc2@example.com")

    private val senderEmail = "sender@example.com"
    private val senderName = "Sender Name"

    private lateinit var emailContactService: EmailContactService

    @BeforeEach
    fun setUp() {
        keycloakUserService = mock()
        emailContactService =
            EmailContactService(
                keycloakUserService,
                internalRecipients,
                internalCcRecipients,
                senderEmail,
                senderName,
            )
    }

    @Test
    fun `getSenderContact should return correct sender EmailContact`() {
        val senderContact = emailContactService.getSenderContact()

        assertEquals(senderEmail, senderContact.emailAddress)
        assertEquals(senderName, senderContact.name)
    }

    @Test
    fun `getContacts should return internalContacts for EmailRecipientInternal`() {
        val recipient = EmailRecipient.Internal
        val result = emailContactService.getContacts(recipient)

        val expectedContacts = internalEmailAddresses.map { EmailContact(it) }
        assertEquals(expectedContacts.size, result.size)
        assertTrue(result.containsAll(expectedContacts))
    }

    @Test
    fun `getContacts should return internalCcContacts for EmailRecipientInternalCc`() {
        val recipient = EmailRecipient.InternalCc
        val result = emailContactService.getContacts(recipient)

        val expectedContacts = internalCcEmailAddresses.map { EmailContact(it) }
        assertEquals(expectedContacts.size, result.size)
        assertTrue(result.containsAll(expectedContacts))
    }

    @Test
    fun `getContacts should return EmailContact for EmailRecipientEmailAddress`() {
        val email = "test@example.com"
        val recipient = EmailRecipient.EmailAddress(email)
        val result = emailContactService.getContacts(recipient)

        assertEquals(listOf(EmailContact(email)), result)
    }

    @Test
    fun `getContacts should return EmailContact for EmailRecipientUserId with valid email`() {
        val keycloakUser = KeycloakUserInfo(userId = "user123", email = "user@example.com", firstName = "John", lastName = "Doe")

        `when`(keycloakUserService.getUser(keycloakUser.userId)).thenReturn(keycloakUser)

        val recipient = EmailRecipient.UserId(keycloakUser.userId)
        val result = emailContactService.getContacts(recipient)

        assertNotNull(keycloakUser.email)
        assertEquals(listOf(EmailContact(keycloakUser.email!!, null)), result)
    }
}
