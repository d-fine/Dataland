package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.elementaryEventProcessing.ElementaryEventBasicInfo
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.services.elementaryEventProcessing.PublicDataUploadProcessor
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import java.util.*

class PublicDataUploadProcessorTest {
    private lateinit var publicDataUploadProcessor: PublicDataUploadProcessor

    @BeforeEach
    fun setup() {
        val messageUtilsMock = mock(MessageQueueUtils::class.java)
        val notificationServiceMock = mock(NotificationService::class.java)
        val elementaryEventRepositoryMock = mock(ElementaryEventRepository::class.java)
        val metaDataControllerApiMock = mock(MetaDataControllerApi::class.java)
        val objectMapper = jacksonObjectMapper()

        publicDataUploadProcessor = PublicDataUploadProcessor(
            messageUtilsMock,
            notificationServiceMock,
            elementaryEventRepositoryMock,
            objectMapper,
            metaDataControllerApiMock,
        )
    }

    @Test
    fun `empty identifier leads to rejection exception`() {
        val payload = JSONObject(
            mapOf("identifier" to ""),
        ).toString()

        val exception = assertThrows<MessageQueueRejectException> {
            publicDataUploadProcessor.validateIncomingPayloadAndReturnDataId(payload, MessageType.QaCompleted)
        }

        assertEquals("Message was rejected: The identifier in the message payload is empty.", exception.message)
    }

    @Test
    fun `non empty dataId leads to valid return of dataId`() {
        val dummyId = "123"
        val payload = JSONObject(
            mapOf("identifier" to dummyId),
        ).toString()

        val dataId = assertDoesNotThrow {
            publicDataUploadProcessor.validateIncomingPayloadAndReturnDataId(payload, MessageType.QaCompleted)
        }

        assertEquals(dummyId, dataId)
    }

    @Test
    fun `create valid elementaryEventBasicInfo`() {
        val dummyCompanyId = UUID.randomUUID()
        val dummyReportingPeriod = "2022"
        val jsonString = JSONObject(
            mapOf(
                "dataId" to "abc",
                "companyId" to dummyCompanyId,
                "dataType" to DataTypeEnum.heimathafen.toString(),
                "reportingPeriod" to dummyReportingPeriod,
            ),
        ).toString()

        val actualElementaryEventBasicInfo = publicDataUploadProcessor.createElementaryEventBasicInfo(jsonString)
        val expectedElementaryEventBasicInfo =
            ElementaryEventBasicInfo(
                companyId = dummyCompanyId,
                framework = DataTypeEnum.heimathafen,
                reportingPeriod = dummyReportingPeriod,
            )

        assertEquals(expectedElementaryEventBasicInfo, actualElementaryEventBasicInfo)
    }
}
