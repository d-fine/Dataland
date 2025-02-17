package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandmessagequeueutils.utils.getCorrelationId
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
import org.springframework.transaction.annotation.Transactional

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
        private val dataPointManager: DataPointManager,
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
        @Transactional
        fun updateQaStatus(messages: List<Message>) {
            logger.info("Processing ${messages.size} Data Point QA Status Updated Messages.")

            MessageQueueUtils.rejectMessageOnException {
                for (message in messages) {
                    val qaStatusChangeMessage =
                        message.readMessagePayload<QaStatusChangeMessage>(objectMapper)
                    val correlationId = message.getCorrelationId()
                    logger.info("Received QA status change message (correlationId: $correlationId)")

                    val updatedDataId = qaStatusChangeMessage.dataId
                    MessageQueueUtils.validateDataId(updatedDataId)
                    val newQaStatus = qaStatusChangeMessage.updatedQaStatus
                    val newActiveDataId = qaStatusChangeMessage.currentlyActiveDataId
                    val dataPointDimension = dataPointMetaInformationManager.getDataPointDimensionFromId(updatedDataId)

                    dataPointMetaInformationManager.updateQaStatusOfDataPoint(updatedDataId, newQaStatus)
                    logger.info("QA status for dataID $updatedDataId updated to $newQaStatus (correlationId: $correlationId)")

                    dataPointManager.updateCurrentlyActiveDataPoint(dataPointDimension, newActiveDataId, correlationId)
                }
            }
        }
    }
