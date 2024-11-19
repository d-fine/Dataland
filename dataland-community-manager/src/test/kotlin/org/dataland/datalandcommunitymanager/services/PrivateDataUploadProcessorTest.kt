package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.elementaryEventProcessing.ElementaryEventBasicInfo
import org.dataland.datalandcommunitymanager.repositories.ElementaryEventRepository
import org.dataland.datalandcommunitymanager.services.elementaryEventProcessing.PrivateDataUploadProcessor
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import java.util.UUID

class PrivateDataUploadProcessorTest {
    private lateinit var privateDataUploadProcessor: PrivateDataUploadProcessor

    @BeforeEach
    fun setup() {
        val notificationServiceMock = mock(NotificationService::class.java)
        val elementaryEventRepositoryMock = mock(ElementaryEventRepository::class.java)
        val objectMapper = jacksonObjectMapper()

        privateDataUploadProcessor =
            PrivateDataUploadProcessor(
                notificationServiceMock,
                elementaryEventRepositoryMock,
                objectMapper,
            )
    }

    @Test
    fun `empty dataId leads to rejection exception`() {
        val payload =
            JSONObject(
                mapOf(
                    "dataId" to "",
                    "actionType" to ActionType.STORE_PRIVATE_DATA_AND_DOCUMENTS,
                ),
            ).toString()

        val exception =
            assertThrows<MessageQueueRejectException> {
                privateDataUploadProcessor.validateIncomingPayloadAndReturnDataId(payload, MessageType.PRIVATE_DATA_RECEIVED)
            }

        assertEquals("Message was rejected: The dataId in the message payload is empty.", exception.message)
    }

    @Test
    fun `unexpected action type leads to rejection exception`() {
        val unexpectedActionType = "unexpected action type"
        val payload =
            JSONObject(
                mapOf(
                    "dataId" to "some-data-Id",
                    "actionType" to unexpectedActionType,
                ),
            ).toString()

        val exception =
            assertThrows<MessageQueueRejectException> {
                privateDataUploadProcessor.validateIncomingPayloadAndReturnDataId(payload, unexpectedActionType)
            }

        assertEquals(
            "Message was rejected: Message has type \"$unexpectedActionType\" but type \"Private Data received\" was expected",
            exception.message,
        )
    }

    @Test
    fun `happy path of successful validation`() {
        val dummyCompanyId = UUID.randomUUID()
        val dummyReportingPeriod = "2022"
        val payload =
            JSONObject(
                mapOf(
                    "dataId" to "abc",
                    "bypassQa" to "false",
                    "companyId" to dummyCompanyId.toString(),
                    "framework" to DataTypeEnum.heimathafen.toString(),
                    "reportingPeriod" to dummyReportingPeriod,
                    "actionType" to ActionType.STORE_PRIVATE_DATA_AND_DOCUMENTS,
                ),
            ).toString()

        assertDoesNotThrow {
            privateDataUploadProcessor.validateIncomingPayloadAndReturnDataId(
                payload,
                MessageType.PRIVATE_DATA_RECEIVED,
            )
        }

        val actualElementaryEventBasicInfo = privateDataUploadProcessor.createElementaryEventBasicInfo(payload)
        val expectedElementaryEventBasicInfo =
            ElementaryEventBasicInfo(dummyCompanyId, DataTypeEnum.heimathafen, dummyReportingPeriod)

        assertEquals(expectedElementaryEventBasicInfo, actualElementaryEventBasicInfo)
    }
}
