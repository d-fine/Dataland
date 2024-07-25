package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.elementaryEventProcessing.ElementaryEventBasicInfo
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.json.JSONObject
import java.util.*

/**
 * Validator to check the contents of payloads in the message queue
 */
object PayloadValidator {

    /**
     * Validates if a message payload from the rabbit mq contains the expected content
     * @param payload the content of the message
     * @param expectedActionType the expected action type in the payload
     * @returns an object that contains meta info about the elementary event associated with the payload
     */
    fun validatePayloadAndReturnElementaryEventBasicInfo(
        payload: String,
        expectedActionType: String,
    ): ElementaryEventBasicInfo {
        val payloadJsonObject = JSONObject(payload)

        val dataId = payloadJsonObject.getString("dataId")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("The dataId in the message payload is empty.")
        }

        val actionType = payloadJsonObject.getString("actionType")
        if (actionType != expectedActionType) {
            throw MessageQueueRejectException(
                "Expected action type $expectedActionType, but was $actionType.",
            )
        }

        val companyId = UUID.fromString(payloadJsonObject.getString("companyId"))
        val framework = payloadJsonObject.getString("framework")
        val frameworkAsEnum = DataTypeEnum.decode(framework)!!
        val reportingPeriod = payloadJsonObject.getString("reportingPeriod")

        return ElementaryEventBasicInfo(
            companyId = companyId,
            framework = frameworkAsEnum,
            reportingPeriod = reportingPeriod,
        )
    }
}
