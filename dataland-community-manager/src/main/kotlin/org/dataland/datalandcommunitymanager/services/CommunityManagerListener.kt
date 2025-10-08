package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.SourceabilityInfo
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.PrivateDataUploadMessage
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.messages.SourceabilityMessage
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
import org.springframework.transaction.annotation.Transactional

/**
 * This service checks if freshly uploaded and validated data answers a data request
 */
@Service("DataRequestUpdater")
class CommunityManagerListener(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val dataRequestUpdateManager: DataRequestUpdateManager,
    @Autowired private val investorRelationsManager: InvestorRelationsManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Checks if, for a given dataset, there are open or nonsourceable requests with matching company identifier,
     * reporting period and data type and sets their status to answered.
     * @param payload the message describing the result of the completed QA process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "dataQualityAssuredCommunityManagerDataManager",
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
    fun changeRequestStatusAfterQaDecision(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) id: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.QA_STATUS_UPDATED)
        val qaStatusChangeMessage = MessageQueueUtils.readMessagePayload<QaStatusChangeMessage>(payload, objectMapper)
        val dataId = qaStatusChangeMessage.dataId
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        logger.info("Received data QA completed message for dataset with ID $dataId")
        if (qaStatusChangeMessage.updatedQaStatus != QaStatus.Accepted) {
            logger.info("Dataset with ID $dataId was not accepted and request matching is cancelled")
            return
        }
        MessageQueueUtils.rejectMessageOnException {
            dataRequestUpdateManager.processUserRequests(
                dataId = dataId,
                correlationId = id,
            )
            investorRelationsManager.saveNotificationEventForInvestorRelationsEmails(
                dataId = dataId,
            )
        }
    }

    /**
     * Checks if, for a given dataset, there are open requests with matching company identifier, reporting period
     * and data type and sets their status to answered and handles the update of the access status
     * @param payload the message body containing the dataId of the uploaded data
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "privateRequestReceivedCommunityManager",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.PRIVATE_REQUEST_RECEIVED, declare = "false"),
                key = [RoutingKeyNames.META_DATA_PERSISTED],
            ),
        ],
    )
    @Transactional
    fun changeRequestStatusAfterPrivateDataUpload(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) id: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.PRIVATE_DATA_RECEIVED)
        val privateDataUploadMessage = MessageQueueUtils.readMessagePayload<PrivateDataUploadMessage>(payload, objectMapper)
        val dataId = privateDataUploadMessage.dataId
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        MessageQueueUtils.rejectMessageOnException {
            dataRequestUpdateManager.processUserRequests(
                dataId,
                correlationId = id,
            )
        }
    }

    /**
     * Checks whether at least one of the fields companyId or reportingPeriod in the message
     * is empty and, if so, throws an appropriate exception.
     */
    private fun checkThatReceivedDataIsComplete(sourceabilityMessage: SourceabilityMessage) {
        if (sourceabilityMessage.companyId.isEmpty() || sourceabilityMessage.reportingPeriod.isEmpty()) {
            throw MessageQueueRejectException("Both companyId and reportingPeriod must be provided.")
        }
    }

    /**
     * Checks whether the message actually corresponds to a dataset being set to non-sourceable
     * (as opposed to it being set to sourceable). If not, it throws an appropriate exception.
     */
    private fun checkThatDatasetWasSetToNonSourceable(sourceabilityMessage: SourceabilityMessage) {
        if (!sourceabilityMessage.isNonSourceable) {
            throw MessageQueueRejectException("Received event did not set a dataset to status non-sourceable.")
        }
    }

    /**
     * Tries to decode the dataType string field in the message to a DataTypeEnum object to return.
     * If the decoding fails, an appropriate exception is thrown.
     */
    private fun decodeDataTypeIfPossible(sourceabilityMessage: SourceabilityMessage): DataTypeEnum =
        DataTypeEnum.decode(sourceabilityMessage.dataType)
            ?: throw MessageQueueRejectException("Framework name could not be understood.")

    /**
     * Listens for message that specifies a dataset as non-sourceable
     * and patches all requests corresponding to this dataset to the request status non-sourceable.
     * @param payload the message describing the result of the data non-sourceable event
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
    fun processMessageForDataReportedAsNonSourceable(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.DATA_NONSOURCEABLE)
        val sourceabilityMessage = MessageQueueUtils.readMessagePayload<SourceabilityMessage>(payload, objectMapper)

        checkThatReceivedDataIsComplete(sourceabilityMessage)
        checkThatDatasetWasSetToNonSourceable(sourceabilityMessage)
        val dataTypeDecoded = decodeDataTypeIfPossible(sourceabilityMessage)

        logger.info(
            "Received data-non-sourceable-message for data type: ${sourceabilityMessage.dataType}, " +
                "company ID: ${sourceabilityMessage.companyId} and reporting period: ${sourceabilityMessage.reportingPeriod}. " +
                "Correlation ID: $correlationId",
        )

        val sourceabilityInfo =
            SourceabilityInfo(
                companyId = sourceabilityMessage.companyId,
                dataType = dataTypeDecoded,
                reportingPeriod = sourceabilityMessage.reportingPeriod,
                isNonSourceable = sourceabilityMessage.isNonSourceable,
                reason = sourceabilityMessage.reason,
            )

        MessageQueueUtils.rejectMessageOnException {
            dataRequestUpdateManager.patchAllNonWithdrawnRequestsToStatusNonSourceable(sourceabilityInfo, correlationId)
        }
    }
}
