package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandcommunitymanager.model.elementaryEventProcessing.ElementaryEventBasicInfo
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.services.elementaryEventProcessing.PublicDataUploadProcessor
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.UUID

class PublicDataUploadProcessorTest {
    private lateinit var publicDataUploadProcessor: PublicDataUploadProcessor

    private val objectMapper = jacksonObjectMapper()

    private lateinit var elementaryEventRepositoryMock: ElementaryEventRepository

    private var dataId = UUID.randomUUID()
    private val activeDataId = UUID.randomUUID()
    private var companyId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        val notificationServiceMock = mock(NotificationService::class.java)
        val metaDataControllerApiMock = mock(MetaDataControllerApi::class.java)

        elementaryEventRepositoryMock = mock(ElementaryEventRepository::class.java)
        `when`(elementaryEventRepositoryMock.saveAndFlush(any())).then { invocation -> invocation.arguments[0] }

        `when`(metaDataControllerApiMock.getDataMetaInfo(any()))
            .thenReturn(
                DataMetaInformation(
                    dataId.toString(), companyId.toString(), DataTypeEnum.heimathafen, 0, "2022", false,
                    org.dataland.datalandbackend.openApiClient.model.QaStatus.Pending, null,
                ),
            )

        publicDataUploadProcessor =
            PublicDataUploadProcessor(
                notificationServiceMock,
                elementaryEventRepositoryMock,
                objectMapper,
                metaDataControllerApiMock,
            )

        publicDataUploadProcessor.notificationFeatureFlagAsString = "true"
    }

    @Test
    fun `create valid elementaryEventBasicInfo`() {
        val dummyReportingPeriod = "2022"
        val jsonString =
            JSONObject(
                mapOf(
                    "dataId" to dataId,
                    "companyId" to companyId,
                    "dataType" to DataTypeEnum.heimathafen.toString(),
                    "reportingPeriod" to dummyReportingPeriod,
                ),
            ).toString()

        val actualElementaryEventBasicInfo = publicDataUploadProcessor.createElementaryEventBasicInfo(jsonString)
        val expectedElementaryEventBasicInfo =
            ElementaryEventBasicInfo(
                companyId = companyId,
                framework = DataTypeEnum.heimathafen,
                reportingPeriod = dummyReportingPeriod,
            )

        assertEquals(expectedElementaryEventBasicInfo, actualElementaryEventBasicInfo)
    }

    @Test
    fun `do not create an elementary event when the dataset has been rejected`() {
        val qaStatusChangeMessage =
            QaStatusChangeMessage(
                dataId = dataId.toString(),
                updatedQaStatus = QaStatus.Rejected,
                currentlyActiveDataId = activeDataId.toString(),
            )
        val payload = objectMapper.writeValueAsString(qaStatusChangeMessage)

        publicDataUploadProcessor.processEvent(payload, "correlationId", MessageType.QA_STATUS_CHANGED)

        Mockito.verifyNoInteractions(elementaryEventRepositoryMock)
    }

    @Test
    fun `create an elementary event when the dataset has been approved`() {
        val qaStatusChangeMessage =
            QaStatusChangeMessage(
                dataId = dataId.toString(),
                updatedQaStatus = QaStatus.Accepted,
                currentlyActiveDataId = dataId.toString(),
            )
        val payload = objectMapper.writeValueAsString(qaStatusChangeMessage)

        publicDataUploadProcessor.processEvent(payload, "correlationId", MessageType.QA_STATUS_CHANGED)

        verify(elementaryEventRepositoryMock, times(1)).saveAndFlush(any())
    }
}
