package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.services.templateemail.DataRequestClosedEmailFactory
import org.dataland.datalandemailservice.utils.assertEmailContactInformationEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class DataRequestClosedEmailFactoryTest {
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
    private val dataRequestId = UUID.randomUUID().toString()
    private val closedInDays = "10"

    private fun buildTestEmail(
        setOptionalProperties: Boolean,
    ): Email {
        var properties = mapOf(
            "companyId" to companyId,
            "companyName" to companyName,
            "dataRequestId" to dataRequestId,
            "dataType" to dataType,
            "reportingPeriod" to reportingPeriod,
            "creationDate" to creationTimestampAsDate,
            "closedInDays" to closedInDays,
        )
        if (setOptionalProperties) {
            properties = properties + mapOf(
                "dataTypeDescription" to dataTypeDescription,
            )
        }

        val email = DataRequestClosedEmailFactory(
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

    private fun validateEmailHtmlFormatContainsDefaultProperties(email: Email) {
        assertTrue(email.content.htmlContent.contains(dataType))
    }
    private fun validateEmailHtmlFormatContainsOptionalProperties(email: Email) {
        assertTrue(email.content.htmlContent.contains(dataTypeDescription))
    }
    private fun validateEmailHtmlFormat(email: Email) {
        assertEmailContactInformationEquals(
            EmailContact(senderEmail, senderName),
            setOf(EmailContact(receiverEmail)),
            emptySet(),
            email,
        )
        assertTrue(email.content.htmlContent.contains("DATALAND"))
        assertTrue(email.content.htmlContent.contains("Your answered data request has been automatically closed"))
        assertTrue(email.content.htmlContent.contains("Copyright"))
        assertTrue(email.content.htmlContent.contains("$closedInDays days."))
        assertTrue(email.content.htmlContent.contains(companyName))
        assertTrue(email.content.htmlContent.contains(reportingPeriod))
        assertTrue(email.content.htmlContent.contains(creationTimestampAsDate))
        assertTrue(
            email.content.htmlContent.contains(
                "href=\"https://$proxyPrimaryUrl/requests/$dataRequestId\"",
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
            email.content.textContent.contains("Your answered data request has been automatically closed "),
        )
        assertTrue(email.content.textContent.contains("Company: $companyName \n"))
        assertTrue(email.content.textContent.contains("Reporting period: $reportingPeriod \n\n"))
        assertTrue(email.content.textContent.contains("Framework: $dataType \n"))
        assertTrue(
            email.content.textContent.contains("Request last modified: $creationTimestampAsDate \n\n"),
        )

        assertTrue(
            email.content.textContent.contains("$proxyPrimaryUrl/requests/$dataRequestId"),
        )
    }
}
