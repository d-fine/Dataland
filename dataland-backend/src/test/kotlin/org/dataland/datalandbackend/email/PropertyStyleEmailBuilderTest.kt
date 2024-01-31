package org.dataland.datalandbackend.email

import org.dataland.datalandemail.email.Email
import org.dataland.datalandemail.email.PropertyStyleEmailBuilder
import org.dataland.datalandemail.utils.assertEmailContactInformationEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PropertyStyleEmailBuilderTest {
    private val senderEmail = "sender@dataland.com"
    private val senderName = "Test"
    private val receiverEmails = listOf(
        "receiver1@dataland.com",
        "receiver2@dataland.com",
    )
    private val ccEmails = listOf(
        "cc1@dataland.com",
    )
    private val subject = "The Subject"
    private val textTitle = "The Text Title"
    private val htmlTitle = "The HTML Title"

    @Test
    fun `validate that the output of the property style email builder is correctly formatted`() {
        val properties = mapOf(
            "first" to "1",
            "leftOut" to null,
            "second" to "2",
        )
        val email = object : PropertyStyleEmailBuilder(
            senderEmail = senderEmail,
            senderName = senderName,
            semicolonSeparatedReceiverEmails = receiverEmails.joinToString(";"),
            semicolonSeparatedCcEmails = ccEmails.joinToString(";"),
        ) {
            fun build(): Email {
                return Email(
                    senderEmailContact,
                    receiverEmailContacts!!,
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
        assertEmailContactInformationEquals(senderEmail, senderName, receiverEmails, ccEmails, email)
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
