package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
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
 * @param messageQueueUtils contains utils to be used to handle messages for the message queue
 * @param dataManager the dataManager service for public data
*/
@Component("MessageQueueListenerForDataManager")
class MessageQueueListenerForDataManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val messageQueueUtils: MessageQueueUtils,
    @Autowired private val dataManager: DataManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

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
        messageQueueUtils.validateMessageType(type, MessageType.DATA_STORED)
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        logger.info(
            "Received message that dataset with dataId $dataId has been successfully stored. Correlation ID: " +
                "$correlationId.",
        )
        messageQueueUtils.rejectMessageOnException {
            dataManager.removeDataSetFromInMemoryStore(dataId)
        }
    }

    /**
     * Method that listens to the qa_queue and changes the qa status and the active data set after successful qa process
     * @param jsonString the message describing the changed QA status process
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
    fun changeQaStatus(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        messageQueueUtils.validateMessageType(type, MessageType.QA_STATUS_CHANGED)
        val qaStatusChangeMessage = objectMapper.readValue(jsonString, QaStatusChangeMessage::class.java)

        val changedQaStatusDataId = qaStatusChangeMessage.changedQaStatusDataId
        val updatedQaStatus = qaStatusChangeMessage.updatedQaStatus
        val currentlyActiveDataId = qaStatusChangeMessage.currentlyActiveDataId

        if (changedQaStatusDataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID to change qa status dataset is empty")
        }
        if (currentlyActiveDataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID to newly active dataset is empty")
        }

        messageQueueUtils.rejectMessageOnException {
            val changedQaStatusMetaInformation =
                metaDataManager.getDataMetaInformationByDataId(changedQaStatusDataId)

            changedQaStatusMetaInformation.qaStatus = updatedQaStatus
            val currentlyActiveMetaInformation =
                metaDataManager.getDataMetaInformationByDataId(currentlyActiveDataId)
            metaDataManager.setActiveDataset(currentlyActiveMetaInformation)

            logger.info(
                "Received quality assurance: ${qaStatusChangeMessage.updatedQaStatus} for data upload with DataId: " +
                    "$changedQaStatusDataId  and set currently active dataset with DataId $currentlyActiveDataId" +
                    "on true with Correlation Id: $correlationId",
            )
        }
    }
}
