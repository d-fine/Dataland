package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestUpdated
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DataRequestResponseEmailSenderTest {
    private var mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()
    private var objectMapper = ObjectMapper()
    private var mockCompanyInfoService = mock<CompanyInfoService>()
    private val dataRequestResponseEmailSender =
        DataRequestResponseEmailSender(
            mockCloudEventMessageHandler,
            mockCompanyInfoService,
            objectMapper,
            staleDaysThreshold = "30",
        )

    @BeforeEach
    fun setUp() {
        Mockito.reset(
            mockCloudEventMessageHandler,
            mockCompanyInfoService,
        )
    }

    @Test
    fun `check that the correct email is sent to a user with a closed data request upon a new QA event`() {
        val dummyDatalandCompanyId = "dataland-company-id"
        val sampleReportingPeriod = "2023"
        val sampleDataTypeLabel = "EU Taxonomy for financial companies"
        val sampleTimeStamp = 1708533324967
        val sampleDate = "21 Feb 2024, 17:35" // corresponds to sampleTimeStamp
        val dummyDataRequestId = "data-request-id"
        val dummyUserId = "user-id"
        val dummyCompanyName = "company-name"

        val mockDataRequestEntity = mock<DataRequestEntity>()
        doReturn(dummyDatalandCompanyId).whenever(mockDataRequestEntity).datalandCompanyId
        doReturn(sampleReportingPeriod).whenever(mockDataRequestEntity).reportingPeriod
        doReturn(sampleDataTypeLabel).whenever(mockDataRequestEntity).getDataTypeDescription()
        doReturn(sampleTimeStamp).whenever(mockDataRequestEntity).creationTimestamp
        doReturn(dummyDataRequestId).whenever(mockDataRequestEntity).dataRequestId
        doReturn(dummyUserId).whenever(mockDataRequestEntity).userId

        val mockCompanyInformation = mock<CompanyInformation>()
        doReturn(dummyCompanyName).whenever(mockCompanyInformation).companyName

        doReturn(dummyCompanyName)
            .whenever(mockCompanyInfoService)
            .checkIfCompanyIdIsValidAndReturnNameOrId(anyOrNull())

        val actualMessage =
            dataRequestResponseEmailSender.buildDataUpdatedEmailMessage(
                mockDataRequestEntity,
            )

        val expectedMessage =
            EmailMessage(
                typedEmailContent =
                    DataRequestUpdated(
                        companyName = dummyCompanyName,
                        dataTypeLabel = sampleDataTypeLabel,
                        reportingPeriod = sampleReportingPeriod,
                        creationDate = sampleDate,
                        dataRequestId = dummyDataRequestId,
                    ),
                receiver = listOf(EmailRecipient.UserId(dummyUserId)),
                cc = listOf(),
                bcc = listOf(),
            )

        assertEquals(
            objectMapper.writeValueAsString(actualMessage),
            objectMapper.writeValueAsString(expectedMessage),
        )
    }
}
