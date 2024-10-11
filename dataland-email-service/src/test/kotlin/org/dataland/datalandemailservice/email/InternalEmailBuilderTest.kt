package org.dataland.datalandemailservice.email

import org.dataland.datalandemailservice.services.InternalEmailBuilder
import org.dataland.datalandemailservice.utils.EmailMatchingPattern
import org.dataland.datalandemailservice.utils.assertEmailMatchesPattern
import org.dataland.datalandemailservice.utils.toEmailContacts
import org.dataland.datalandmessagequeueutils.messages.InternalEmailMessage
import org.junit.jupiter.api.Test

class InternalEmailBuilderTest {
    private val senderEmail = "sender@example.com"
    private val senderName = "Test"
    private val receiverEmails = listOf("receiver1@example.com", "receiver2@dexample.com")
    private val ccEmails = listOf("cc1@example.com")
    private val environment = "test.dataland.com"
    private val originalProperty = "something"

    @Test
    fun `validate that the internal email builder adds the environment property`() {
        val message =
            InternalEmailMessage(
                subject = "SUBJECT",
                textTitle = "Text Title",
                htmlTitle = "HTML Title",
                properties = mapOf("Original" to originalProperty),
            )
        val email =
            InternalEmailBuilder(
                proxyPrimaryUrl = environment,
                senderEmail = senderEmail,
                senderName = senderName,
                semicolonSeparatedReceiverEmails = receiverEmails.joinToString(";"),
                semicolonSeparatedCcEmails = ccEmails.joinToString(";"),
            ).buildInternalEmail(message)
        assertEmailMatchesPattern(
            email,
            EmailMatchingPattern(
                expectedSender = EmailContact(senderEmail, senderName),
                expectedReceiversGetter = { receiverEmails.toEmailContacts() },
                expectedCc = ccEmails.toEmailContacts(),
                expectedSubject = "SUBJECT",
                expectedToBeContainedInTextContent = setOf("Environment: $environment", "Original: $originalProperty"),
                expectedToBeContainedInHtmlContent = setOf(),
            ),
        )
    }
}
