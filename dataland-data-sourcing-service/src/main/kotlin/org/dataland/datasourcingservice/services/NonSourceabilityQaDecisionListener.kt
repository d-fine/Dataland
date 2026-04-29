package org.dataland.datasourcingservice.services

import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
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
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Handles the QA decision events for non-sourceability emitted by the QA service.
 *
 * On QA_ACCEPTED: transitions the matching data sourcing object to [DataSourcingState.NonSourceable].
 * On QA_REJECTED: sets the data sourcing object in [DataSourcingState.DocumentSourcingDone].
 */
@Service
class NonSourceabilityQaDecisionListener(
    @Autowired private val dataSourcingQueryManager: DataSourcingQueryManager,
    @Autowired private val dataSourcingManager: DataSourcingManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles an incoming non-sourceability QA decision event (accepted or rejected) from the QA service.
     * Validates the message type, deserializes the payload, and delegates to [processQaDecisionEvent].
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.DATA_SOURCING_SERVICE_NON_SOURCEABILITY_QA_DECISION,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.QA_SERVICE_NON_SOURCEABILITY_DECISIONS),
                key = [RoutingKeyNames.NON_SOURCEABILITY_QA_DECISION],
            ),
        ],
    )
    fun onNonSourceabilityQaDecision(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) messageType: String,
    ) {
        MessageQueueUtils.rejectMessageOnException {
            if (messageType != MessageType.NON_SOURCEABILITY_QA_ACCEPTED &&
                messageType != MessageType.NON_SOURCEABILITY_QA_REJECTED
            ) {
                throw MessageQueueRejectException(
                    "Unexpected message type \"$messageType\" in non-sourceability QA decision listener",
                )
            }
            val event = MessageQueueUtils.readMessagePayload<NonSourceabilityLifecycleEvent>(payload)
            processQaDecisionEvent(event, messageType)
        }
    }

    @Transactional
    internal fun processQaDecisionEvent(
        event: NonSourceabilityLifecycleEvent,
        messageType: String,
    ) {
        when (messageType) {
            MessageType.NON_SOURCEABILITY_QA_ACCEPTED -> {
                transitionToNonSourceable(event)
            }

            MessageType.NON_SOURCEABILITY_QA_REJECTED -> {
                transitionToDocumentSourcingDone(event)
            }

            else -> {
                logger.error(
                    "Unexpected message type $messageType in NonSourceabilityQaDecisionListener " +
                        ". Discarding.",
                )
                throw MessageQueueRejectException("Unexpected message type $messageType in QA decision listener")
            }
        }
    }

    private fun transitionToNonSourceable(event: NonSourceabilityLifecycleEvent) {
        val sourcing = findSourcingForEvent(event) ?: return
        if (sourcing.state == DataSourcingState.NonSourceable) {
            logger.info("Idempotent skip: already NonSourceable for nonSourceabilityId=${event.nonSourceabilityId}")
            return
        }
        dataSourcingManager.patchDataSourcingEntityById(
            UUID.fromString(sourcing.dataSourcingId),
            DataSourcingPatch(state = DataSourcingState.NonSourceable),
        )
        logger.info(
            "Transitioned dataSourcingId=${sourcing.dataSourcingId} to NonSourceable via QA accepted " +
                "(nonSourceabilityId=${event.nonSourceabilityId})",
        )
    }

    @Transactional
    internal fun transitionToDocumentSourcingDone(event: NonSourceabilityLifecycleEvent) {
        val sourcing = findSourcingForEvent(event) ?: return
        if (sourcing.state == DataSourcingState.DocumentSourcingDone) {
            logger.info("Idempotent skip: already in DocumentSourcingDone for nonSourceabilityId=${event.nonSourceabilityId}")
            return
        }
        dataSourcingManager.patchDataSourcingEntityById(
            UUID.fromString(sourcing.dataSourcingId),
            DataSourcingPatch(state = DataSourcingState.DocumentSourcingDone),
        )
        logger.info(
            "Transitioned dataSourcingId=${sourcing.dataSourcingId} to DocumentSourcingDone " +
                "(nonSourceabilityId=${event.nonSourceabilityId})",
        )
    }

    private fun findSourcingForEvent(event: NonSourceabilityLifecycleEvent) =
        dataSourcingQueryManager
            .searchDataSourcings(
                companyId = UUID.fromString(event.companyId),
                dataType = event.dataType,
                reportingPeriod = event.reportingPeriod,
                state = null,
                chunkSize = 1,
                chunkIndex = 0,
            ).firstOrNull()
            .also { sourcing ->
                if (sourcing == null) {
                    logger.info(
                        "No data sourcing object found for companyId=${event.companyId}, dataType=${event.dataType}, " +
                            "reportingPeriod=${event.reportingPeriod} (" +
                            "nonSourceabilityId=${event.nonSourceabilityId}). Skipping.",
                    )
                }
            }
}
