package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
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
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param metaDataManager service for managing metadata
 * @param dataManager the dataManager service for public data
*/
@Component("MessageQueueListenerForDataManager")
class MessageQueueListenerForDataManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val dataManager: DataManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method that listens to the qa_queue and updates the metadata information after successful qa process
     * @param jsonString the message describing the result of the completed QA process
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "dataQualityAssuredBackendDataManager",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.DATA_QUALITY_ASSURED, declare = "false"),
                key = [RoutingKeyNames.DATA],
            ),
        ],
    )
    @Transactional
    fun updateMetaData(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.QA_COMPLETED)

        val qaCompletedMessage = MessageQueueUtils.readMessagePayload<QaCompletedMessage>(jsonString, objectMapper)
        val dataId = qaCompletedMessage.identifier
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        MessageQueueUtils.rejectMessageOnException {
            val metaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
            metaInformation.qaStatus = qaCompletedMessage.validationResult
            if (qaCompletedMessage.validationResult == QaStatus.Accepted) {
                metaDataManager.setActiveDataset(metaInformation)
            }
            logger.info(
                "Received quality assurance: ${qaCompletedMessage.validationResult} for data upload with DataId: " +
                    "$dataId with Correlation Id: $correlationId",
            )
        }
    }

    /**
     * Method that listens to the stored queue and removes data entries from the temporary storage once they have been
     * stored in the persisted database. Further it logs success notification associated containing dataId and
     * correlationId
     * @param dataId the ID of the dataset to that was stored
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "dataStoredBackendDataManager",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.ITEM_STORED, declare = "false"),
                key = [RoutingKeyNames.DATA],
            ),
        ],
    )
    fun removeStoredItemFromTemporaryStore(
        @Payload dataId: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.DATA_STORED)
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        logger.info(
            "Received message that dataset with dataId $dataId has been successfully stored. Correlation ID: " +
                "$correlationId.",
        )
        MessageQueueUtils.rejectMessageOnException {
            dataManager.removeDataSetFromInMemoryStore(dataId)
        }
    }
}
