package org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.logging.CorrelationLogging
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
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

/**
 * Listens to non-sourceability-created events from the backend and creates a corresponding
 * [NonSourceableQaReviewInformationEntity] in the QA service (FR-004).
 */
@Service
class NonSourceabilityEventListener(
    @Autowired private val nonSourceableQaReviewRepository: NonSourceableQaReviewRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Handles a non-sourceability-created event from the backend and creates a pending
     * [NonSourceableQaReviewInformationEntity] for review.
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.QA_SERVICE_NON_SOURCEABILITY_CREATED,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.BACKEND_DATA_NONSOURCEABLE, declare = "false"),
                key = [RoutingKeyNames.NON_SOURCEABILITY_CREATED],
            ),
        ],
    )
    fun onNonSourceabilityCreated(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) messageType: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.rejectMessageOnException {
            MessageQueueUtils.validateMessageType(messageType, MessageType.NON_SOURCEABILITY_CREATED)
            val event = MessageQueueUtils.readMessagePayload<NonSourceabilityLifecycleEvent>(payload)
            CorrelationLogging.withNonSourceabilityContext(correlationId, event.nonSourceabilityId) {
                processCreatedEvent(event, correlationId)
            }
        }
    }

    @Transactional
    internal fun processCreatedEvent(
        event: NonSourceabilityLifecycleEvent,
        correlationId: String,
    ) {
        if (event.eventType != NonSourceabilityEventType.NON_SOURCEABILITY_CREATED) {
            throw MessageQueueRejectException(
                "Unexpected event type ${event.eventType} in QA NonSourceabilityEventListener",
            )
        }

        val existing = nonSourceableQaReviewRepository.findByNonSourceabilityId(event.nonSourceabilityId)
        if (existing != null) {
            logger.info(
                "Idempotent skip: QA review record already exists for nonSourceabilityId=${event.nonSourceabilityId} " +
                    "(correlationId=$correlationId)",
            )
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
                uploaderUserId = "",
                uploadTime = Instant.now().toEpochMilli(),
            )
        nonSourceableQaReviewRepository.save(entity)
        logger.info(
            "Created QA review record for nonSourceabilityId=${event.nonSourceabilityId} (correlationId=$correlationId)",
        )
    }
}
