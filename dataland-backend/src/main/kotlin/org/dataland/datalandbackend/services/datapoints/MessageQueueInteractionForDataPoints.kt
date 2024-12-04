package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.data.DataUploadedPayload
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Component that bundles the message queue interactions of the data pint manager
 * @param cloudEventMessageHandler cloud event message handler used for sending messages to the message queue
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
*/

@Component
class MessageQueueInteractionForDataPoints(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to publish a message that a data point has been uploaded
     * @param dataId The ID of the uploaded data point
     * @param correlationId The correlation ID of the request initiating the event
     */
    fun publishDataPointUploadedMessage(
        dataId: String,
        bypassQa: Boolean,
        correlationId: String,
    ) {
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(DataUploadedPayload(dataId = dataId, bypassQa = bypassQa)),
            type = MessageType.PUBLIC_DATA_RECEIVED,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATA_POINT_EVENTS,
            routingKey = RoutingKeyNames.DATA_POINT_UPLOAD,
        )
        logger.info("Published message that data point with ID '$dataId' has been uploaded. Correlation ID: '$correlationId'.")
    }
}
