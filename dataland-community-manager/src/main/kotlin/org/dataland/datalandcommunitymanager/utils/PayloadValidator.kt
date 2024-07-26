package org.dataland.datalandcommunitymanager.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandcommunitymanager.model.elementaryEventProcessing.ElementaryEventBasicInfo
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

/**
 * Can parse and validate payloads
 */
@Service("PayloadValidator")
class PayloadValidator(
    @Autowired private val objectMapper: ObjectMapper,
) {

    /**
     * Validates if a message payload from the rabbit mq contains the expected content for messages associated
     * with data uploads.
     * It throws execptions if a validation rule is violated.
     * @param payload the content of the message
     * @param expectedActionType the expected action type in the payload
     */
    fun validatePayloadOfDataUploadMessage(
        payload: String,
        expectedActionType: String,
    ) {
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
    }

    /**
     * Parses a message payload from the rabbit mq as object.
     * The object mapper itself throws errors if parsing is not possible.
     * @param payload the content of the message
     * @returns an object that contains basic info about the elementary event associated with the payload
     */
    fun parseElementaryEventBasicInfo(payload: String): ElementaryEventBasicInfo {
        val temporaryObjectMapper = objectMapper.copy()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        return temporaryObjectMapper.readValue(
            payload,
            ElementaryEventBasicInfo::class.java,
        )
    }
}
