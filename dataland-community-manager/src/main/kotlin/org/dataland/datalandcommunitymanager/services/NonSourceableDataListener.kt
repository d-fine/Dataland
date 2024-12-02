package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.NonSourceableInfo
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

/**
 * This service processes new messages of non-sourceable data
 */
@Service("NonSourceableDataListener")
class NonSourceableDataListener(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val nonSourceableDataManager: NonSourceableDataManager,
) {
    private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    /**
     * Listens for information that specifies a dataset as non-sourceable
     * and patches all requests corresponding to this dataset to the request status non-sourceable.
     * @param jsonString the message describing the result of the data non-sourceable event
     * @param type the type of the message
     * @param correlationId the correlation id of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "community-manager.queue.nonSourceableData",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.BACKEND_DATA_NONSOURCEABLE, declare = "false"),
                key = [RoutingKeyNames.DATA_NONSOURCEABLE],
            ),
        ],
    )
    fun processDataReportedNotSourceableMessage(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.DATA_NONSOURCEABLE)
        val nonSourceableInfo = MessageQueueUtils.readMessagePayload<NonSourceableInfo>(jsonString, objectMapper)
        if (nonSourceableInfo.dataType.toString().isEmpty() ||
            nonSourceableInfo.companyId.isEmpty() ||
            nonSourceableInfo.reportingPeriod.isEmpty()
        ) {
            throw MessageQueueRejectException("Received data is incomplete")
        }

        if (!nonSourceableInfo.isNonSourceable) {
            throw MessageQueueRejectException("Received event did not set a dataset to status non-sourceable")
        }
        logger.info(
            "Received data-non-sourceable-message for data type: ${nonSourceableInfo.dataType} " +
                "company ID: ${nonSourceableInfo.companyId} and reporting period: ${nonSourceableInfo.reportingPeriod}. " +
                "Correlation ID: $correlationId",
        )

        MessageQueueUtils.rejectMessageOnException {
            nonSourceableDataManager.patchAllRequestsForThisDatasetToStatusNonSourceable(nonSourceableInfo, correlationId)
        }
    }
}
