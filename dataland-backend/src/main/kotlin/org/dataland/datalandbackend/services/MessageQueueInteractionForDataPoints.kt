package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.messages.data.DataUploadPayload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param metaDataManager service for managing metadata
*/

@Component("MessageQueueInteractionForDataPoints")
class MessageQueueInteractionForDataPoints(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val metaDataManager: DataMetaInformationManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to publish a message that a data point has been uploaded
     * @param dataId The ID of the uploaded data point
     * @param bypassQa Whether the data point should be sent to QA or not
     * @param correlationId The correlation ID of the request initiating the event
     */
    fun publishDataPointUploadedMessage(
        dataId: UUID,
        bypassQa: Boolean,
        correlationId: String,
    ) {
        val payload =
            objectMapper.writeValueAsString(
                DataUploadPayload(
                    dataId = dataId,
                    bypassQa = bypassQa,
                ),
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = payload,
            type = MessageType.PUBLIC_DATA_RECEIVED,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATA_POINT_EVENTS,
        )
        logger.info("Published message to queue that data point with ID '$dataId' has been uploaded. Correlation ID: '$correlationId'.")
    }
}
