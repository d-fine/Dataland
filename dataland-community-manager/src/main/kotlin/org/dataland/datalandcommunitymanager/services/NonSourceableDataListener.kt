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
    private val dataRequestAlterationManager: DataRequestAlterationManager,
) {
    private val logger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    /**
     * Checks if for a given dataset there are open requests with matching company identifier, reporting period
     * and data type and sets their status to answered
     * @param jsonString the message describing the result of the data non-sourceable event
     * @param type the type of the message
     * @param id the correlation id of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "nonSourceableData",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.DATA_NONSOURCEABLE, declare = "false"),
                key = [RoutingKeyNames.DATA_NONSOURCEABLE],
            ),
        ],
    )
    fun changeRequestStatusAfterDataReportedNonSourceable(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) id: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.DATA_NONSOURCEABLE)
        val nonSourceableInfo = MessageQueueUtils.readMessagePayload<NonSourceableInfo>(jsonString, objectMapper)
        if (nonSourceableInfo.dataType.toString().isEmpty() ||
            nonSourceableInfo.companyId.isEmpty() ||
            nonSourceableInfo.reportingPeriod.isEmpty()
        ) {
            throw MessageQueueRejectException("Provided data is not empty")
        }
        logger.info("Received request status changed to non-sourceable for company ID: ${nonSourceableInfo.companyId}")
        if (!nonSourceableInfo.nonSourceable) {
            logger.info("Event did not set a dataset to non-sourceable")
            return
        }
        MessageQueueUtils.rejectMessageOnException {
            dataRequestAlterationManager.patchAllRequestsForThisDatasetToStatusNonSourceable(nonSourceableInfo, correlationId = id)
        }
    }
}
