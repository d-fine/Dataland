package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
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
import org.springframework.stereotype.Service

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param dataPointMetaInformationManager service for managing metadata
 */
@Service
class MessageQueueListenerForDataPointManager
    @Autowired
    constructor(
        private val objectMapper: ObjectMapper,
        private val dataPointMetaInformationManager: DataPointMetaInformationManager,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Method that listens to the messages from the QA service, modifies the qa status in the metadata accordingly,
         * and updates which dataset is currently active after successful qa process
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            QueueNames.BACKEND_DATA_POINT_QA_STATUS_UPDATED,
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS, declare = "false"),
                    key = [RoutingKeyNames.DATA_POINT_QA],
                ),
            ],
            containerFactory = "consumerBatchContainerFactory",
        )
        fun updateQaStatus(messages: List<Message>) {
            logger.info("Processing ${messages.size} Data Point QA Status Updated Messages.")

            MessageQueueUtils.rejectMessageOnException {
                val qaStatusChangedMessages =
                    messages.map {
                        MessageQueueUtils.validateMessageType(it.getType(), MessageType.QA_STATUS_UPDATED)
                        val qaStatusChangeMessage =
                            it.readMessagePayload<QaStatusChangeMessage>(objectMapper)
                        val correlationId = it.getCorrelationId()
                        logger.info(
                            "Updating QA status for dataId ${qaStatusChangeMessage.dataId} to " +
                                "${qaStatusChangeMessage.updatedQaStatus} (correlationId: $correlationId)",
                        )
                        Pair(qaStatusChangeMessage, correlationId)
                    }
                val lastMessagePerDataId = qaStatusChangedMessages.associateBy { it.first.dataId }
                dataPointMetaInformationManager.updateQaStatusOfDataPointsFromMessages(lastMessagePerDataId.values.map { it.first })

                val dataPointDimensions =
                    dataPointMetaInformationManager.getDataPointDimensionsFromIds(
                        qaStatusChangedMessages.map { it.first.dataId },
                    )
                val lastMessagePerDataPointDimensions =
                    qaStatusChangedMessages.associateBy {
                        dataPointDimensions[it.first.dataId]
                            ?: error(
                                "Data point dimensions not found for dataId ${it.first.dataId}. " +
                                    "This should be impossible.",
                            )
                    }

                dataPointMetaInformationManager.updateCurrentlyActiveDataPointBulk(
                    lastMessagePerDataPointDimensions.map {
                        DataPointMetaInformationManager.UpdateCurrentlyActiveDataPointTask(
                            dataPointDimensions = it.key,
                            newActiveDataId = it.value.first.currentlyActiveDataId,
                            correlationId = it.value.second,
                        )
                    },
                )
            }
        }
    }
