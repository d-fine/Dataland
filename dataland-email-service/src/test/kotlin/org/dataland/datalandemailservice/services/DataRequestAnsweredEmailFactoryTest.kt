package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.services.templateemail.DataRequestAnsweredEmailFactory
import org.dataland.datalandemailservice.utils.assertEmailContactInformationEquals
import org.dataland.datalandemailservice.utils.getProperties
import org.dataland.datalandemailservice.utils.validateEmailHtmlFormatContainsDefaultProperties
import org.dataland.datalandemailservice.utils.validateEmailHtmlFormatContainsOptionalProperties
import org.dataland.datalandemailservice.utils.validateHtmlContentOfBasicRequestResponseProperties
import org.dataland.datalandemailservice.utils.validateTextContentOfBasicRequestResponseProperties
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DataRequestAnsweredEmailFactoryTest {
    private val proxyPrimaryUrl = "local-dev.dataland.com"
    private val senderEmail = "sender@dataland.com"
    private val senderName = "Dataland"
    private val receiverEmail = "user@testemail.com"

    private fun buildTestEmail(setOptionalProperties: Boolean): Email {
        val properties = getProperties(setOptionalProperties)

        val email =
            DataRequestAnsweredEmailFactory(
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
    fun `validate that the html output of the data request answered mail is correctly formatted`() {
        val basicEmail = buildTestEmail(false)
        val emailWithOptionalProperties = buildTestEmail(true)

        validateEmailHtmlFormat(basicEmail)
        validateEmailHtmlFormatContainsDefaultProperties(basicEmail)

        validateEmailHtmlFormatContainsOptionalProperties(emailWithOptionalProperties)
        validateEmailHtmlFormat(emailWithOptionalProperties)
    }

    private fun validateEmailHtmlFormat(email: Email) {
        assertEmailContactInformationEquals(
            EmailContact(senderEmail, senderName),
            setOf(EmailContact(receiverEmail)),
            emptySet(),
            email,
        )

        assertTrue(email.content.htmlContent.contains("Great news!"))
        assertTrue(email.content.htmlContent.contains("Your data request has been answered."))
        assertTrue(email.content.htmlContent.contains("How to proceed?"))
        assertTrue(email.content.htmlContent.contains("Review the provided data."))
        assertTrue(email.content.htmlContent.contains("Close or reopen your data request."))

        validateHtmlContentOfBasicRequestResponseProperties(email)
    }

    @Test
    fun `validate that the default text content of the data request answered mail is correctly formatted`() {
        val basicEmail = buildTestEmail(false)
        val emailWithOptionalProperties = buildTestEmail(true)
        validateTextOfDefaultEmail(basicEmail)
        validateTextOfDefaultEmail(emailWithOptionalProperties)
    }

    private fun validateTextOfDefaultEmail(email: Email) {
        validateTextContentOfBasicRequestResponseProperties(email)

        assertTrue(
            email.content.textContent.contains("Great news!\nYour data request has been answered.\n\n"),
        )
        assertTrue(email.content.textContent.contains("Review the provided data:\n"))
        assertTrue(
            email.content.textContent.contains(
                "\nWithout any actions, your data request will be set to closed automatically ",
            ),
        )
    }
}
