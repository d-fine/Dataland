package org.dataland.datalandbackend.services

import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param messageQueueUtils holds util methods to handle messages
 * @param privateDataManager the datamanager service for private manager
*/
@Component("MessageQueueListenerForPrivateDataManager")
class MessageQueueListenerForPrivateDataManager(
    @Autowired private val messageQueueUtils: MessageQueueUtils,
    @Autowired private val privateDataManager: PrivateDataManager,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * This method processes a storing request if the applicable message is received from the queue
     * @param payload the paylod of the received message from the message queue
     * @param correlationId the correlationId of the request
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "dataStoredBackendPrivateDataManager",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.PRIVATE_ITEM_STORED, declare = "false"),
                key = [RoutingKeyNames.DATA],
            ),
        ],
    )
    fun processStoredPrivateVsmeData(
        @Payload payload: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        messageQueueUtils.validateMessageType(type, MessageType.PRIVATE_DATA_STORED)
        val dataId = messageQueueUtils.getDataId(payload)
        logger.info(
            "Received message that dataset with dataId $dataId and correlationId $correlationId was successfully " +
                "stored on EuroDaT. Starting to persist mapping info, meta info and clearing in-memory-storages",
        )
        messageQueueUtils.rejectMessageOnException {
            privateDataManager.persistMappingInfo(dataId, correlationId)
            val metaData = privateDataManager.persistMetaInfo(dataId, correlationId)
            privateDataManager.removeRelatedEntriesFromInMemoryStorages(dataId, correlationId)
            val outboundPayload =
                JSONObject(
                    mapOf(
                        "dataId" to dataId,
                        "actionType" to ActionType.STORE_PRIVATE_DATA_AND_DOCUMENTS,
                        "companyId" to metaData.company.companyId,
                        "framework" to metaData.dataType,
                        "reportingPeriod" to metaData.reportingPeriod,
                    ),
                ).toString()
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                outboundPayload, MessageType.PRIVATE_DATA_RECEIVED, correlationId,
                ExchangeName.PRIVATE_REQUEST_RECEIVED, RoutingKeyNames.META_DATA_PERSISTED,
            )
            logger.info(
                "Persisting of meta data information is done. Sending out message for dataId $dataId and " +
                    "correlationId $correlationId .",
            )
        }
    }
}
