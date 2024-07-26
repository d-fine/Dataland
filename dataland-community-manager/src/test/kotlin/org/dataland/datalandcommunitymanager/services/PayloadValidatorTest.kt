package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.elementaryEventProcessing.ElementaryEventBasicInfo
import org.dataland.datalandcommunitymanager.utils.PayloadValidator
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class PayloadValidatorTest {

    private val payloadValidator = PayloadValidator(jacksonObjectMapper())

    @Test
    fun `empty dataId leads to rejection exception`() {
        val payload = JSONObject(
            mapOf("dataId" to ""),
        ).toString()

        val exception = assertThrows<MessageQueueRejectException> {
            payloadValidator.validatePayloadOfDataUploadMessage(payload, "")
        }

        assertEquals("Message was rejected: The dataId in the message payload is empty.", exception.message)
    }

    @Test
    fun `unexpected action type leads to rejection exception`() {
        val unexpectedActionType = "unexpected action type"
        val expectedActionType = "expected"
        val payload = JSONObject(
            mapOf(
                "dataId" to "some-data-Id",
                "actionType" to unexpectedActionType,
            ),
        ).toString()

        val exception = assertThrows<MessageQueueRejectException> {
            payloadValidator.validatePayloadOfDataUploadMessage(payload, expectedActionType)
        }

        assertEquals(
            "Message was rejected: Expected action type $expectedActionType, but was $unexpectedActionType.",
            exception.message,
        )
    }

    @Test
    fun `happy path of successful validation`() {
        val dummyCompanyId = UUID.randomUUID()
        val dummyReportingPeriod = "2022"
        val payload = JSONObject(
            mapOf(
                "dataId" to "abc",
                "bypassQa" to "false",
                "companyId" to dummyCompanyId.toString(),
                "framework" to DataTypeEnum.heimathafen.toString(),
                "reportingPeriod" to dummyReportingPeriod,
                "actionType" to ActionType.StorePublicData,
            ),
        ).toString()
        assertDoesNotThrow { payloadValidator.validatePayloadOfDataUploadMessage(payload, ActionType.StorePublicData) }
        val actualElementaryEventBasicInfo = payloadValidator.parseElementaryEventBasicInfo(payload)
        val expectedElementaryEventBasicInfo =
            ElementaryEventBasicInfo(dummyCompanyId, DataTypeEnum.heimathafen, dummyReportingPeriod)

        assertEquals(expectedElementaryEventBasicInfo, actualElementaryEventBasicInfo)
    }
}
