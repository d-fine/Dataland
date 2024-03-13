package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.services.templateemail.DataRequestAnsweredEmailFactory
import org.dataland.datalandemailservice.utils.assertEmailContactInformationEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DataRequestAnsweredEmailFactoryTest {
    private val proxyPrimaryUrl = "local-dev.dataland.com"
    private val senderEmail = "sender@dataland.com"
    private val senderName = "Dataland"
    private val companyName = "Test Inc."
    private val reportingPeriod = "2022"
    private val companyId = "59f05156-e1ba-4ea8-9d1e-d4833f6c7afc"
    private val receiverEmail = "user@testemail.com"
    private val dataType = "sfdr"
    private val dataTypeDescription = "SFDR"
    private val creationTimestampAsDate = "07 Mar 2024, 15:03"
    private val closedInDays = "100 days"

    private fun buildTestEmail(
        setOptionalProperties: Boolean,
    ): Email {
        var properties = mapOf(
            "companyId" to companyId,
            "companyName" to companyName,
            "dataType" to dataType,
            "reportingPeriod" to reportingPeriod,
            "creationDate" to creationTimestampAsDate,
        )
        if (setOptionalProperties) {
            properties = properties + mapOf(
                "closedIn" to closedInDays,
                "dataTypeDescription" to dataTypeDescription,
            )
        }

        val email = DataRequestAnsweredEmailFactory(
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

        validateEmailHtmlFormat(emailWithOptionalProperties)
        validateEmailHtmlFormatContainsOptionalProperties(emailWithOptionalProperties)
    }

    private fun validateEmailHtmlFormatContainsDefaultProperties(email: Email){
        assertTrue(email.content.htmlContent.contains("some days"))
    }
    private fun validateEmailHtmlFormatContainsOptionalProperties(email: Email){
        assertTrue(email.content.htmlContent.contains(closedInDays))
        assertTrue(email.content.htmlContent.contains(dataTypeDescription))
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

        assertTrue(email.content.htmlContent.contains(companyName))
        assertTrue(email.content.htmlContent.contains(dataType))
        assertTrue(email.content.htmlContent.contains(reportingPeriod))
        assertTrue(email.content.htmlContent.contains(creationTimestampAsDate))
        assertTrue(
            email.content.htmlContent.contains(
                "href=\"https://$proxyPrimaryUrl/companies/$companyId/frameworks/$dataType\"",
            ),
        )
    }

    @Test
    fun `validate that the default text content of the data request answered mail is correctly formatted`() {
        val basicEmail = buildTestEmail(false)
        val emailWithOptionalProperties = buildTestEmail(true)
        validateTextOfDefaultEmail(basicEmail)
        validateTextOfDefaultEmail(emailWithOptionalProperties)
    }

    private fun validateTextOfDefaultEmail(email: Email) {
        assertTrue(
            email.content.textContent.contains("Great news!\nYour data request has been answered.\n\n"),
        )
        assertTrue(email.content.textContent.contains("Company: $companyName \n"))
        assertTrue(email.content.textContent.contains("Reporting period: $reportingPeriod \n\n"))
        assertTrue(email.content.textContent.contains("Framework: $dataType \n"))
        assertTrue(
            email.content.textContent.contains("Request created: $creationTimestampAsDate \n\n"),
        )
        assertTrue(email.content.textContent.contains("Go to your data requests:\n"))
        assertTrue(
            email.content.textContent.contains("$proxyPrimaryUrl/companies/$companyId/frameworks/$dataType"),
        )
        assertTrue(
            email.content.textContent.contains(
                "\nWithout any actions, your data request will be set to closed automatically in some days.",
            ),
        )
    }
}
