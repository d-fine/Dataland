package org.dataland.datalandbackend.email

import org.dataland.datalandbackendutils.email.BaseEmailBuilder
import org.dataland.datalandbackendutils.email.Email
import org.dataland.datalandbackendutils.email.EmailContact
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
        val subject = "The Subject"
        val textTitle = "The Text Title"
        val htmlTitle = "The HTML Title"
        val properties = mapOf(
            "first" to "1",
            "leftOut" to null,
            "second" to "2",
        )
        val email = object : BaseEmailBuilder(
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
                        subject,
                        textTitle,
                        htmlTitle,
                        properties,
                    ),
                )
            }
        }.build()

        assertEquals(EmailContact(senderEmail, senderName), email.sender)
        assertEquals(receiverEmails.size, email.receivers.size)
        receiverEmails.forEachIndexed { index, it ->
            assertEquals(EmailContact(it), email.receivers[index])
        }
        assertEquals(ccEmails.size, email.cc!!.size)
        ccEmails.forEachIndexed { index, it ->
            assertEquals(EmailContact(it), email.cc!![index])
        }
        assertTrue(email.content.textContent.contains(textTitle))
        assertTrue(!email.content.textContent.contains(htmlTitle))
        assertTrue(!email.content.htmlContent.contains(textTitle))
        assertTrue(email.content.htmlContent.contains("<div class=\"header\">$htmlTitle</div>"))
        properties.filter { it.value != null }.forEach {
            assertTrue(email.content.textContent.contains("${it.key}: ${it.value}"))
            assertTrue(
                email.content.htmlContent.contains(
                    """
                <div class="section"> <span class="bold">${it.key}: </span> ${it.value} </div>
                    """.trimIndent(),
                ),
            )
        }
    }
}
