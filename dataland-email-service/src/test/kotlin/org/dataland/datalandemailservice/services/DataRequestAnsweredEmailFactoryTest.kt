package org.dataland.datalandemailservice.services

import org.dataland.datalandemailservice.email.Email
import org.dataland.datalandemailservice.email.EmailContact
import org.dataland.datalandemailservice.services.templateemail.DataRequestAnsweredEmailFactory
import org.dataland.datalandemailservice.utils.assertEmailContactInformationEquals
import org.junit.jupiter.api.Assertions
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
            "dataTypeDescription" to dataTypeDescription,
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
    fun `validate that the output of the data request answered mail is correctly formatted`() {
        val emailWithoutOptionalProperties = buildTestEmail(false)
        val emailWithOptionalProperties = buildTestEmail(true)
        validateEmailFormat(emailWithoutOptionalProperties, false)
        validateEmailFormat(emailWithOptionalProperties, true)
    }
    private fun validateEmailFormat(email: Email, hasOptionalProperties: Boolean) {
        assertEmailContactInformationEquals(
            EmailContact(senderEmail, senderName),
            setOf(EmailContact(receiverEmail)),
            emptySet(),
            email,
        )
        Assertions.assertTrue(email.content.htmlContent.contains(companyName))
        Assertions.assertTrue(email.content.htmlContent.contains(dataType))
        Assertions.assertTrue(email.content.htmlContent.contains(reportingPeriod))
        Assertions.assertTrue(email.content.htmlContent.contains(creationTimestampAsDate))
        Assertions.assertTrue(
            email.content.htmlContent.contains(
                "href=\"https://$proxyPrimaryUrl/companies/$companyId/frameworks/$dataType\"",
            ),
        )
        if (hasOptionalProperties) {
            Assertions.assertTrue(email.content.htmlContent.contains(closedInDays))
            Assertions.assertTrue(email.content.htmlContent.contains(dataTypeDescription))
        } else {
            Assertions.assertTrue(email.content.htmlContent.contains("some days"))
        }
    }

    @Test
    fun `validate that the text content of the data request answered mail is correctly formatted`() {
        val emailWithoutOptionalProperties = buildTestEmail(false)
        val emailWithOptionalProperties = buildTestEmail(true)
        validateEmailText(emailWithoutOptionalProperties)
        validateEmailText(emailWithOptionalProperties)
    }
    private fun validateEmailText(email: Email) {
        Assertions.assertTrue(
            email.content.textContent.contains(
                "Great news!\nYour data request has been answered.\n\n",
            ),
        )
        Assertions.assertTrue(
            email.content.textContent.contains(
                "Company: $companyName \n",
            ),
        )
        Assertions.assertTrue(
            email.content.textContent.contains(
                "Go to your data requests:\n",
            ),
        )
        Assertions.assertTrue(
            email.content.textContent.contains(
                "Request created: $creationTimestampAsDate \n\n",
            ),
        )
        Assertions.assertTrue(
            email.content.textContent.contains(
                "$proxyPrimaryUrl/companies/$companyId/frameworks/$dataType",
            ),
        )

        Assertions.assertTrue(
            email.content.textContent.contains(
                "\nWithout any actions, your data request will be set to closed automatically in some days.",
            ),
        )

        Assertions.assertTrue(email.content.textContent.contains("Framework: $dataType \n"))
    }
}
