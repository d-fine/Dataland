package org.dataland.datalandbackend.email

import org.dataland.datalandbackendutils.email.BaseEmailBuilder
import org.dataland.datalandbackendutils.email.Email
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BaseEmailBuilderTest {
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
        val properties = mapOf(
            "first" to "1",
            "second" to "2",
        )
        val generatedEmail = object : BaseEmailBuilder(
            senderEmail = senderEmail,
            senderName = senderName,
            semicolonSeparatedReceiverEmails = receiverEmails.joinToString(";"),
            semicolonSeparatedCcEmails = ccEmails.joinToString(";"),
        ) {
            fun build(): Email {
                return Email(
                    senderEmailContact,
                    receiverEmailContacts,
                    ccEmailContacts,
                    buildPropertyStyleEmailContent(
                        "SUBJECT",
                        "TEXT TITLE",
                        "HTML TITLE",
                        properties,
                    )
                )
            }
        }.build()

        assertEquals(senderEmail, generatedEmail.sender.emailAddress)
        assertEquals(senderName, generatedEmail.sender.name)
    }
}