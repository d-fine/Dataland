package org.dataland.datalandemailservice.email

import org.dataland.datalandemailservice.services.InternalEmailBuilder
import org.dataland.datalandemailservice.utils.assertEmailContactInformationEquals
import org.dataland.datalandemailservice.utils.toEmailContacts
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InternalEmailBuilderTest {
    private val senderEmail = "sender@dataland.com"
    private val senderName = "Test"
    private val receiverEmails = listOf("receiver1@dataland.com", "receiver2@dataland.com")
    private val ccEmails = listOf("cc1@dataland.com")
    private val environment = "test.dataland.com"
    private val originalProperty = "something"

    @Test
    fun `validate that the the internal email builder adds the environment property`() {
        val message = InternalEmailMessage(
            subject = "SUBJECT",
            textTitle = "Text Title",
            htmlTitle = "HTML Title",
            properties = mapOf("Original" to originalProperty),
        )
        val email = InternalEmailBuilder(
            proxyPrimaryUrl = environment,
            senderEmail = senderEmail,
            senderName = senderName,
            semicolonSeparatedReceiverEmails = receiverEmails.joinToString(";"),
            semicolonSeparatedCcEmails = ccEmails.joinToString(";"),
        ).buildInternalEmail(message)
        assertEmailContactInformationEquals(
            EmailContact(senderEmail, senderName),
            receiverEmails.toEmailContacts(),
            ccEmails.toEmailContacts(),
            email,
        )
        assertTrue(email.content.textContent.contains("Environment: $environment"))
        assertTrue(email.content.textContent.contains("Original: $originalProperty"))
    }
}
