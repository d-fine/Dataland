package org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReviewManager
import org.dataland.datalandqaservice.repositories.QaReviewRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.model.QaStatus as BackendQaStatus

@SpringBootTest(
    classes = [DatalandQaService::class],
    properties = ["spring.profiles.active=containerized-db"],
)
class QaReviewManagerQaChangeTest(
    @Autowired private val qaReviewManager: QaReviewManager,
    @Autowired private val qaReviewRepository: QaReviewRepository,
    @Autowired private val objectMapper: ObjectMapper,
) : BaseIntegrationTest() {
    @MockitoBean
    lateinit var metaDataControllerApi: MetaDataControllerApi

    @MockitoBean
    lateinit var companyDataControllerApi: CompanyDataControllerApi

    @MockitoBean
    lateinit var cloudEventMessageHandler: CloudEventMessageHandler

    private val dataId1 = UUID.randomUUID().toString()
    private val dataId2 = UUID.randomUUID().toString()
    private val companyId = UUID.randomUUID().toString()
    private val correlationId = UUID.randomUUID().toString()
    private val triggeringUserId = UUID.randomUUID().toString()
    private val companyName = "Test Company"
    private val framework = "sfdr"
    private val reportingPeriod = "2023"

    @BeforeEach
    fun setupMocks() {
        val mockCompanyInformation = mock<CompanyInformation> { on { companyName } doReturn "Test Company" }
        val mockStoredCompany = mock<StoredCompany> { on { companyInformation } doReturn mockCompanyInformation }
        val mockDataMetaInformation =
            mock<DataMetaInformation> {
                on { this.companyId } doReturn companyId
                on { dataType } doReturn DataTypeEnum.sfdr
                on { reportingPeriod } doReturn "2023"
                on { qaStatus } doReturn BackendQaStatus.Pending
            }

        whenever(metaDataControllerApi.getDataMetaInfo(dataId1)).thenReturn(mockDataMetaInformation)
        whenever(metaDataControllerApi.getDataMetaInfo(dataId2)).thenReturn(mockDataMetaInformation)
        whenever(companyDataControllerApi.getCompanyById(companyId)).thenReturn(mockStoredCompany)
    }

    private fun storePendingQaReviewEntry(dataId: String): QaReviewEntity =
        qaReviewRepository.save(
            QaReviewEntity(
                dataId = dataId,
                companyId = this.companyId,
                companyName = this.companyName,
                framework = this.framework,
                reportingPeriod = reportingPeriod,
                timestamp = System.currentTimeMillis(),
                qaStatus = QaStatus.Pending,
                triggeringUserId = this.triggeringUserId,
                comment = null,
            ),
        )

    private fun sendQaChange(
        dataId: String,
        qaStatus: QaStatus,
    ) {
        qaReviewManager.handleQaChange(
            dataId = dataId,
            qaStatus = qaStatus,
            triggeringUserId = triggeringUserId,
            comment = null,
            correlationId = correlationId,
        )
    }

    @Test
    fun `test reject single accepted dataset`() {
        storePendingQaReviewEntry(this.dataId1)

        sendQaChange(dataId1, QaStatus.Accepted)
        sendQaChange(dataId1, QaStatus.Rejected)

        val messageBodyCaptor = argumentCaptor<String>()
        verify(cloudEventMessageHandler, times(2)).buildCEMessageAndSendToQueue(
            messageBodyCaptor.capture(),
            any(),
            any(),
            any(),
            any(),
        )

        val message2 = objectMapper.readValue<QaStatusChangeMessage>(messageBodyCaptor.secondValue)
        assert(message2.currentlyActiveDataId == null)
    }

    @Test
    fun `test reject first of two accepted datasets`() {
        storePendingQaReviewEntry(this.dataId1)
        storePendingQaReviewEntry(this.dataId2)

        sendQaChange(dataId1, QaStatus.Accepted)
        sendQaChange(dataId2, QaStatus.Accepted)
        sendQaChange(dataId1, QaStatus.Rejected)

        val messageBodyCaptor = argumentCaptor<String>()
        verify(cloudEventMessageHandler, times(3)).buildCEMessageAndSendToQueue(
            messageBodyCaptor.capture(),
            any(),
            any(),
            any(),
            any(),
        )

        val message3 = objectMapper.readValue<QaStatusChangeMessage>(messageBodyCaptor.thirdValue)
        assert(message3.currentlyActiveDataId == dataId2)
    }

    @Test
    fun `test reject second of two accepted datasets`() {
        storePendingQaReviewEntry(this.dataId1)
        storePendingQaReviewEntry(this.dataId2)

        sendQaChange(dataId1, QaStatus.Accepted)
        sendQaChange(dataId2, QaStatus.Accepted)
        sendQaChange(dataId2, QaStatus.Rejected)

        val messageBodyCaptor = argumentCaptor<String>()
        verify(cloudEventMessageHandler, times(3)).buildCEMessageAndSendToQueue(
            messageBodyCaptor.capture(),
            any(),
            any(),
            any(),
            any(),
        )

        val message3 = objectMapper.readValue<QaStatusChangeMessage>(messageBodyCaptor.thirdValue)
        assert(message3.currentlyActiveDataId == dataId1)
    }
}
