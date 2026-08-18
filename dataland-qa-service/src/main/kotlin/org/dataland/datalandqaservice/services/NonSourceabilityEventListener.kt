package org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.dataland.datalandqaservice.repositories.NonSourceableQaReviewRepository
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
import java.time.Instant
import java.util.UUID

/**
 * Handles non-sourceability submission events from the backend.
 *
 * A single queue binds to [RoutingKeyNames.NON_SOURCEABILITY_SUBMISSION] and dispatches on
 * messageType:
 *   - [MessageType.NON_SOURCEABILITY_CREATED]       → [DataSourcingState.NonSourceableVerification]
 *   - [MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED] → [DataSourcingState.NonSourceable]
 *
 * Fail-fast validation: events with malformed or blank nonSourceabilityId are
 * discarded with an error log and a [MessageQueueRejectException].
 */
@Service
class NonSourceabilityEventListener(
    @Autowired
    private val nonSourceableQaReviewRepository: NonSourceableQaReviewRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.QA_SERVICE_NON_SOURCEABILITY_SUBMISSION,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.BACKEND_DATA_NONSOURCEABLE, declare = "false"),
                key = [RoutingKeyNames.NON_SOURCEABILITY_SUBMISSION],
            ),
        ],
    )
    fun onNonSourceabilitySubmission(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) messageType: String,
    ) {
        MessageQueueUtils.rejectMessageOnException {
            validateMessageType(messageType)

            val event = MessageQueueUtils.readMessagePayload<NonSourceabilityLifecycleEvent>(payload)

            validateNonSourceabilityId(event.nonSourceabilityId)

            when (messageType) {
                MessageType.NON_SOURCEABILITY_CREATED -> {
                    processCreatedEvent(event)
                }

                MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED -> {
                    processAutoAcceptedEvent(event)
                }
            }
        }
    }

    /**
     * Creates a pending QA review for a normal non-sourceability event.
     */
    @Transactional
    internal fun processCreatedEvent(event: NonSourceabilityLifecycleEvent) {
        val existing = nonSourceableQaReviewRepository.findByNonSourceabilityId(event.nonSourceabilityId)

        if (existing != null) {
            logger.info("Idempotent skip: QA review record already exists for nonSourceabilityId=${event.nonSourceabilityId}")
            return
        }

        val entity =
            NonSourceableQaReviewInformationEntity(
                nonSourceabilityId = event.nonSourceabilityId,
                companyId = event.companyId,
                dataType = event.dataType,
                reportingPeriod = event.reportingPeriod,
                qaStatus = QaStatus.Pending,
                reason = null,
                uploaderUserId = event.uploaderUserId,
                uploadTime = Instant.now().toEpochMilli(),
            )

        nonSourceableQaReviewRepository.save(entity)

        logger.info("Created QA review record for nonSourceabilityId=${event.nonSourceabilityId}")
    }

    /**
     * Creates an already-accepted QA review for an auto-accepted non-sourceability event
     * (i.e. one that bypassed manual QA review), so that the QA service retains a
     * consistent, queryable audit record for every non-sourceability claim.
     *
     * No reviewer is set, since no human made this decision. No QA decision message is
     * emitted, since the auto-accept was already communicated by the backend and handled
     * directly by other downstream consumers.
     */
    @Transactional
    internal fun processAutoAcceptedEvent(event: NonSourceabilityLifecycleEvent) {
        val existing = nonSourceableQaReviewRepository.findByNonSourceabilityId(event.nonSourceabilityId)

        if (existing != null) {
            logger.info("Idempotent skip: QA review record already exists for nonSourceabilityId=${event.nonSourceabilityId}")
            return
        }

        val entity =
            NonSourceableQaReviewInformationEntity(
                nonSourceabilityId = event.nonSourceabilityId,
                companyId = event.companyId,
                dataType = event.dataType,
                reportingPeriod = event.reportingPeriod,
                qaStatus = QaStatus.Accepted,
                reason = null,
                uploaderUserId = event.uploaderUserId,
                uploadTime = Instant.now().toEpochMilli(),
                reviewerUserId = null,
                qaComment = "Auto-accepted (QA bypass)",
            )

        nonSourceableQaReviewRepository.save(entity)

        logger.info("Created auto-accepted QA review record for nonSourceabilityId=${event.nonSourceabilityId}")
    }

    /**
     * Accepts only the two non-sourceability lifecycle events handled by this listener.
     */
    private fun validateMessageType(messageType: String) {
        if (messageType != MessageType.NON_SOURCEABILITY_CREATED &&
            messageType != MessageType.NON_SOURCEABILITY_AUTO_ACCEPTED
        ) {
            throw MessageQueueRejectException("Unexpected message type \"$messageType\" in NonSourceabilityEventListener")
        }
    }

    /**
     * Validates that the event contains a usable non-sourceability ID.
     */
    private fun validateNonSourceabilityId(nonSourceabilityId: String) {
        if (nonSourceabilityId.isBlank()) {
            throw MessageQueueRejectException("Received event with blank nonSourceabilityId. Discarding.")
        }

        try {
            UUID.fromString(nonSourceabilityId)
        } catch (exception: IllegalArgumentException) {
            logger.error("Malformed nonSourceabilityId='$nonSourceabilityId'. Discarding.", exception)

            throw MessageQueueRejectException("Malformed nonSourceabilityId='$nonSourceabilityId'", exception)
        }
    }
}
