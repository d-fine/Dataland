package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.messages.data.DataIdPayload
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandmessagequeueutils.utils.getCorrelationId
import org.dataland.datalandmessagequeueutils.utils.getType
import org.dataland.datalandmessagequeueutils.utils.readMessagePayload
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
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
 * @param metaDataManager service for managing metadata
 * @param dataManager the dataManager service for public data
 */
@Component("MessageQueueListenerForDataManager")
class MessageQueueListenerForDataManager(
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val dataManager: DataManager,
    @Autowired private val sourceabilityDataManager: SourceabilityDataManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method that listens to the messages from the QA service, modifies the qa status in the metadata accordingly,
     * and updates which dataset is currently active after successful qa process
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
                exchange = Exchange(ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS, declare = "false"),
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
        MessageQueueUtils.validateMessageType(type, MessageType.QA_STATUS_UPDATED)

        val qaStatusChangeMessage =
            MessageQueueUtils.readMessagePayload<QaStatusChangeMessage>(jsonString)

        val updatedDataId = qaStatusChangeMessage.dataId
        val updatedQaStatus = qaStatusChangeMessage.updatedQaStatus
        val currentlyActiveDataId = qaStatusChangeMessage.currentlyActiveDataId

        logger.info(
            "Received QA Status Change message for dataID $updatedDataId. New qaStatus is $updatedQaStatus. " +
                "(correlationId: $correlationId)",
        )

        if (updatedDataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID to change qa status dataset is empty")
        }

        MessageQueueUtils.rejectMessageOnException {
            val updatedDataMetaInformation = metaDataManager.getDataMetaInformationByDataId(updatedDataId)
            updatedDataMetaInformation.qaStatus = updatedQaStatus
            metaDataManager.storeDataMetaInformation(updatedDataMetaInformation)

            if (currentlyActiveDataId.isNullOrEmpty()) {
                logger.info(
                    "No active dataset passed for companyId ${updatedDataMetaInformation.company.companyId}, " +
                        "dataType ${updatedDataMetaInformation.dataType}, and " +
                        "reportingPeriod ${updatedDataMetaInformation.reportingPeriod}. Setting currently active" +
                        "dataset to inactive.",
                )
                metaDataManager
                    .setCurrentlyActiveDatasetInactive(
                        updatedDataMetaInformation.company,
                        updatedDataMetaInformation.dataType,
                        updatedDataMetaInformation.reportingPeriod,
                    )
            } else {
                val currentlyActiveMetaInformation =
                    metaDataManager.getDataMetaInformationByDataId(currentlyActiveDataId)
                logger.info("Set dataset with dataId $currentlyActiveDataId to active.")
                metaDataManager.setActiveDataset(currentlyActiveMetaInformation)
                logger.info("Check if dataset was previously marked as non-sourceable and if so, mark as sourceable.")
                storeUpdatedDataToNonSourceableData(updatedDataMetaInformation)
            }
        }
    }

    /**
     * Method that listens to the stored queue and removes data entries from the temporary storage once they have been
     * stored in the persisted database. Further it logs success notification associated containing dataId and
     * correlationId
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.BACKEND_DATA_PERSISTED,
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
        containerFactory = "consumerBatchContainerFactory",
    )
    fun removeStoredItemsFromTemporaryStore(messages: List<Message>) {
        logger.info("Processing ${messages.size} Data Point Received Messages.")
        MessageQueueUtils.rejectMessageOnException {
            for (message in messages) {
                MessageQueueUtils.validateMessageType(message.getType(), MessageType.DATA_STORED)
                val dataId = message.readMessagePayload<DataIdPayload>().dataId
                val correlationId = message.getCorrelationId()
                MessageQueueUtils.validateDataId(dataId)
                logger
                    .info("Received message that dataset with dataId $dataId has been successfully stored. Correlation ID: $correlationId.")
                dataManager.removeDatasetFromInMemoryStore(dataId)
            }
        }
    }

    /**
     * Adds a new entry to the data-sourceability repo if a corresponding dataset was previously flagged as
     * non-sourceable.
     * @param updatedDataMetaInformation DataMetaInformationEntity that holds information of the updated dataset.
     */
    private fun storeUpdatedDataToNonSourceableData(updatedDataMetaInformation: DataMetaInformationEntity) {
        if (sourceabilityDataManager
                .getLatestSourceabilityInfoForDataset(
                    updatedDataMetaInformation.company.companyId,
                    DataType.valueOf(updatedDataMetaInformation.dataType),
                    updatedDataMetaInformation.reportingPeriod,
                )?.isNonSourceable == true
        ) {
            sourceabilityDataManager.storeSourceableData(
                updatedDataMetaInformation.company.companyId,
                DataType.valueOf(updatedDataMetaInformation.dataType),
                updatedDataMetaInformation.reportingPeriod,
                updatedDataMetaInformation.uploaderUserId,
            )
        }
    }
}
