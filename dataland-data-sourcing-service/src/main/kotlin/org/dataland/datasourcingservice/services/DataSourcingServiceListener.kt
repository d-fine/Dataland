package org.dataland.datasourcingservice.services

import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingPatch
import org.dataland.datasourcingservice.model.enums.DataSourcingState
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
import java.util.UUID

/**
 * Listener class for all RabbitMQ messages that are relevant to the data sourcing service.
 */
@Service("DataSourcingServiceListener")
class DataSourcingServiceListener
    @Autowired
    constructor(
        private val metaDataControllerApi: MetaDataControllerApi,
        private val dataSourcingManager: DataSourcingManager,
        private val dataSourcingQueryManager: DataSourcingQueryManager,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Listener method that processes messages from the QA service indicating a QA status change of a dataset.
         * @param payload the message payload as a string
         * @param type the message type from the header
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            QueueNames.DATA_SOURCING_SERVICE_DATASET_QA_STATUS_UPDATE,
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
        fun processHandleDatasetUploadEvent(
            @Payload payload: String,
            @Header(MessageHeaderKey.TYPE) type: String,
        ) {
            MessageQueueUtils.validateMessageType(type, MessageType.QA_STATUS_UPDATED)
            val dataUploadedPayload = MessageQueueUtils.readMessagePayload<QaStatusChangeMessage>(payload)
            val dataId = dataUploadedPayload.dataId
            val updatedQaStatus = dataUploadedPayload.updatedQaStatus
            val dataMetaInformation = metaDataControllerApi.getDataMetaInfo(dataId)
            val companyId = UUID.fromString(dataMetaInformation.companyId)
            val reportingPeriod = dataMetaInformation.reportingPeriod
            val dataType = dataMetaInformation.dataType

            val storedDataSourcing =
                dataSourcingQueryManager
                    .searchDataSourcings(
                        companyId = companyId,
                        dataType = dataType.toString(),
                        reportingPeriod = reportingPeriod,
                        state = null,
                        chunkSize = 1,
                        chunkIndex = 0,
                    ).firstOrNull()

            if (storedDataSourcing == null) {
                logger.info(
                    "Received QA status update message for dataset with ID $dataId. However, no data sourcing " +
                        "object exists for the associated company ID $companyId, reporting period $reportingPeriod and " +
                        "data type $dataType.",
                )
                return
            }

            when (updatedQaStatus) {
                QaStatus.Accepted -> {
                    dataSourcingManager.patchDataSourcingEntityById(
                        UUID.fromString(storedDataSourcing.dataSourcingId),
                        DataSourcingPatch(state = DataSourcingState.Done),
                    )
                }

                QaStatus.Pending -> {
                    if (storedDataSourcing.state == DataSourcingState.Done) return
                    dataSourcingManager.patchDataSourcingEntityById(
                        UUID.fromString(storedDataSourcing.dataSourcingId),
                        DataSourcingPatch(state = DataSourcingState.DataVerification),
                    )
                }

                else -> Unit
            }
        }
    }
