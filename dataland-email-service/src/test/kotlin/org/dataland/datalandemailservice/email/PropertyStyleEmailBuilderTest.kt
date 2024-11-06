package org.dataland.datalandemailservice.email

import org.dataland.datalandemailservice.utils.assertEmailContactInformationEquals
import org.dataland.datalandemailservice.utils.toEmailContacts
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PropertyStyleEmailBuilderTest {
    private val senderEmail = "sender@example.com"
    private val senderName = "Test"
    private val receiverEmails =
        listOf(
            "receiver1@example.com",
            "receiver2@example.com",
        )
    private val ccEmails =
        listOf(
            "cc1@example.com",
        )
    private val subject = "The Subject"
    private val textTitle = "The Text Title"
    private val htmlTitle = "The HTML Title"

    @Test
    fun `validate that the output of the property style email builder is correctly formatted`() {
        val properties =
            mapOf(
                "first" to "1",
                "leftOut" to null,
                "second" to "2",
            )
        val email =
            object : PropertyStyleEmailBuilder(
                senderEmail = senderEmail,
                senderName = senderName,
                semicolonSeparatedReceiverEmails = receiverEmails.joinToString(";"),
                semicolonSeparatedCcEmails = ccEmails.joinToString(";"),
            ) {
                fun build(): Email =
                    Email(
                        senderEmailContact,
                        receiverEmailContacts!!,
                        ccEmailContacts ?: emptyList(),
                        listOf(),
                        buildPropertyStyleEmailContent(subject, textTitle, htmlTitle, properties),
                    )
            }.build()
        assertEmailContactInformationEquals(
            EmailContact(senderEmail, senderName),
            receiverEmails.toEmailContacts(),
            ccEmails.toEmailContacts(),
            email,
        )
        validateEmailContent(properties, email)
    }

    private fun validateEmailContent(
        expectedProperties: Map<String, String?>,
        email: Email,
    ) {
        assertTrue(email.content.textContent.contains(textTitle))
        assertTrue(!email.content.textContent.contains(htmlTitle))
        assertTrue(!email.content.htmlContent.contains(textTitle))
        assertTrue(email.content.htmlContent.contains("<div class=\"header\">$htmlTitle</div>"))
        expectedProperties.filter { it.value != null }.forEach {
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
