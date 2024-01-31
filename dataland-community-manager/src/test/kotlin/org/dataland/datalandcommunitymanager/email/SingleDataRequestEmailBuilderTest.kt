package org.dataland.datalandbackend.email

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.DatalandCommunityManager
import org.dataland.datalandcommunitymanager.services.CompanyGetter
import org.dataland.datalandcommunitymanager.services.SingleDataRequestEmailBuilder
import org.dataland.datalandemail.utils.assertEmailContactInformationEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DatalandCommunityManager::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class SingleDataRequestEmailBuilderTest(
    @Autowired val companyGetter: CompanyGetter,
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
        `when`(mockCompanyGetter.getCompanyInfo(any())).thenReturn(
            mock(CompanyInformation::class.java).also { `when`(it.companyName).thenReturn(companyName) },
        )
        val email = SingleDataRequestEmailBuilder(
            senderEmail = senderEmail,
            senderName = senderName,
            companyGetter = companyGetter,
        ).buildSingleDataRequestEmail(
            requesterEmail = requesterEmail,
            receiverEmail = receiverEmail,
            companyId = companyId,
            dataType = DataTypeEnum.lksg,
            reportingPeriods = reportingPeriods,
            message = message,
        )

        assertEmailContactInformationEquals(senderEmail, senderName, listOf(receiverEmail), emptyList(), email)
        assertTrue(email.content.htmlContent.contains(receiverEmail))
        assertTrue(email.content.htmlContent.contains(companyName))
        assertTrue(email.content.htmlContent.contains("LkSG"))
        assertTrue(email.content.htmlContent.contains(reportingPeriods.joinToString(", ")))
        assertTrue(
            email.content.htmlContent.contains(
                "href=\"https://local-dev.dataland.com/companies/$companyId\"",
            ),
        )
    }

    // TODO test optional requester
    // TODO test optional message
}
