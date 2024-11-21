package org.dataland.datalandexternalstorage.services

import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
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
import java.sql.SQLException

/**
 * Simple implementation of the message queue listeners for the euroDatdataManager
 * @param cloudEventMessageHandler service for managing CloudEvents messages on eurodat
 * @param eurodatDataStore service for handling data for the eurodat storage
 */
@Component
class MessageQueueListenerEurodatDataStore(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var eurodatDataStore: EurodatDataStore,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method that listens to the storage_queue and stores data into the EuroDaT database in case there is a message
     * on the storage_queue
     * @param payload the content of the message
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "privateRequestReceivedEurodatDataStore",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.PRIVATE_REQUEST_RECEIVED, declare = "false"),
                key = [RoutingKeyNames.PRIVATE_DATA_AND_DOCUMENT],
            ),
        ],
    )
    fun processStorageRequest(
        @Payload payload: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.PRIVATE_DATA_RECEIVED)
        val dataId = JSONObject(payload).getString("dataId")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty.")
        }
        logger.info(
            "Received storage request for dataId $dataId and correlationId $correlationId with payload: $payload",
        )
        MessageQueueUtils.rejectMessageOnException {
            val actionType = JSONObject(payload).getString("actionType")
            if (actionType == ActionType.STORE_PRIVATE_DATA_AND_DOCUMENTS) {
                try {
                    eurodatDataStore.storeDataInEurodat(dataId, correlationId, payload)
                    eurodatDataStore.sendMessageAfterSuccessfulStorage(payload, correlationId)
                } catch (ex: SQLException) {
                    logger.error("A sql exception was thrown: $ex")
                }
            }
        }
    }
}
