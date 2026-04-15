package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.PrivateDataUploadMessage
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
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
@Service("CommunityManagerListener")
class CommunityManagerListener(
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
        val qaStatusChangeMessage = MessageQueueUtils.readMessagePayload<QaStatusChangeMessage>(payload)
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
        val privateDataUploadMessage = MessageQueueUtils.readMessagePayload<PrivateDataUploadMessage>(payload)
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

    private fun validateLifecycleEvent(event: NonSourceabilityLifecycleEvent) {
        if (event.companyId.isBlank() || event.reportingPeriod.isBlank()) {
            throw MessageQueueRejectException("Both companyId and reportingPeriod must be provided.")
        }
    }

    /**
     * Handles bypassQa=true activations and patches matching requests to non-sourceable.
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.COMMUNITY_MANAGER_NON_SOURCEABILITY_AUTO_ACCEPTED,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.BACKEND_DATA_NONSOURCEABLE, declare = "false"),
                key = [RoutingKeyNames.NON_SOURCEABILITY_AUTO_ACCEPTED],
            ),
        ],
    )
    fun processNonSourceabilityAutoAcceptedEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED)
        val event = MessageQueueUtils.readMessagePayload<NonSourceabilityLifecycleEvent>(payload)
        validateLifecycleEvent(event)
        if (event.eventType != NonSourceabilityEventType.NON_SOURCEABILITY_AUTO_ACCEPTED) {
            throw MessageQueueRejectException("Unexpected eventType ${event.eventType} for auto-accepted listener.")
        }
        MessageQueueUtils.rejectMessageOnException {
            dataRequestUpdateManager.patchAllNonWithdrawnRequestsToStatusNonSourceable(
                companyId = event.companyId,
                dataTypeAsString = event.dataType,
                reportingPeriod = event.reportingPeriod,
                correlationId = correlationId,
            )
        }
    }

    /**
     * Handles QA accepted activations and patches matching requests to non-sourceable.
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.COMMUNITY_MANAGER_NON_SOURCEABILITY_QA_ACCEPTED,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.QA_SERVICE_NON_SOURCEABILITY_DECISIONS, declare = "false"),
                key = [RoutingKeyNames.NON_SOURCEABILITY_QA_ACCEPTED],
            ),
        ],
    )
    fun processNonSourceabilityQaAcceptedEvent(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.NON_SOURCEABILITY_QA_ACCEPTED)
        val event = MessageQueueUtils.readMessagePayload<NonSourceabilityLifecycleEvent>(payload)
        validateLifecycleEvent(event)
        if (event.eventType != NonSourceabilityEventType.NON_SOURCEABILITY_QA_ACCEPTED) {
            throw MessageQueueRejectException("Unexpected eventType ${event.eventType} for QA-accepted listener.")
        }
        MessageQueueUtils.rejectMessageOnException {
            dataRequestUpdateManager.patchAllNonWithdrawnRequestsToStatusNonSourceable(
                companyId = event.companyId,
                dataTypeAsString = event.dataType,
                reportingPeriod = event.reportingPeriod,
                correlationId = correlationId,
            )
        }
    }
}
