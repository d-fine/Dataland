package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.services.templateemail.DataRequestClosedEmailFactory
import org.dataland.datalandemailservice.utils.assertEmailContactInformationEquals
import org.dataland.datalandemailservice.utils.getProperties
import org.dataland.datalandemailservice.utils.validateEmailHtmlFormatContainsDefaultProperties
import org.dataland.datalandemailservice.utils.validateEmailHtmlFormatContainsOptionalProperties
import org.dataland.datalandemailservice.utils.validateHtmlContentOfBasicRequestResponseProperties
import org.dataland.datalandemailservice.utils.validateTextContentOfBasicRequestResponseProperties
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DataRequestClosedEmailFactoryTest {
    private val proxyPrimaryUrl = "local-dev.dataland.com"
    private val senderEmail = "sender@dataland.com"
    private val senderName = "Dataland"
    private val receiverEmail = "user@testemail.com"

    private fun buildTestEmail(setOptionalProperties: Boolean): Email {
        val properties = getProperties(setOptionalProperties)

        val email =
            DataRequestClosedEmailFactory(
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
    fun `validate that the html output of the data request closed mail is correctly formatted`() {
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
        assertTrue(email.content.htmlContent.contains("Your answered data request has been automatically closed"))

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
            email.content.textContent.contains("Your answered data request has been automatically closed "),
        )
        assertTrue(
            email.content.textContent.contains("as no action was taken within the last "),
        )
    }
}
