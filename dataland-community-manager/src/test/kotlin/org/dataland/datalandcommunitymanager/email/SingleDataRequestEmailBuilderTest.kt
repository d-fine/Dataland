package org.dataland.datalandbackend.email

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.DatalandCommunityManager
import org.dataland.datalandcommunitymanager.services.CompanyGetter
import org.dataland.datalandcommunitymanager.services.SingleDataRequestEmailBuilder
import org.dataland.datalandemail.email.EmailContact
import org.dataland.datalandemail.utils.assertEmailContactInformationEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DatalandCommunityManager::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class SingleDataRequestEmailBuilderTest(
    @Value("\${dataland.proxy.primary.url}") private val proxyPrimaryUrl: String,
) {

    private val requesterEmail = "requester@dataland.com"
    private val senderEmail = "sender@dataland.com"
    private val senderName = "Test"
    private val receiverEmail = "receiver1@dataland.com"
    private val companyId = "8"
    private val companyName = "Test Inc."
    private val reportingPeriods = listOf("2022", "2023")
    private val message = "This is a comment"

    @Test
    fun `validate that the output of the single data request email builder is correctly formatted`() {
        val mockCompanyGetter = mock(CompanyGetter::class.java)
        val mockCompanyInformation = mock(CompanyInformation::class.java)
        `when`(mockCompanyInformation.companyName).thenReturn(companyName)
        `when`(mockCompanyGetter.getCompanyInfo(anyString())).thenReturn(mockCompanyInformation)
        val email = SingleDataRequestEmailBuilder(
            proxyPrimaryUrl = proxyPrimaryUrl,
            senderEmail = senderEmail,
            senderName = senderName,
            companyGetter = mockCompanyGetter,
        ).buildSingleDataRequestEmail(
            requesterEmail = requesterEmail,
            receiverEmail = receiverEmail,
            companyId = companyId,
            dataType = DataTypeEnum.lksg,
            reportingPeriods = reportingPeriods,
            message = message,
        )
        assertEmailContactInformationEquals(
            EmailContact(senderEmail, senderName),
            setOf(EmailContact(receiverEmail)),
            emptySet(),
            email
        )
        println(email.content.htmlContent)
        assertTrue(email.content.htmlContent.contains(requesterEmail))
        assertTrue(email.content.htmlContent.contains(companyName))
        assertTrue(email.content.htmlContent.contains("LkSG"))
        assertTrue(email.content.htmlContent.contains(reportingPeriods.joinToString(", ")))
        println(email.content.htmlContent)
        assertTrue(
            email.content.htmlContent.contains(
                "href=\"$proxyPrimaryUrl/companies/$companyId\"",
            ),
        )
    }
}
