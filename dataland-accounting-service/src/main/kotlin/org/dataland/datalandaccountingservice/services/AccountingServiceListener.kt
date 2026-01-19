package org.dataland.datalandaccountingservice.services
import org.dataland.datalandaccountingservice.entities.BilledRequestEntity
import org.dataland.datalandaccountingservice.model.BilledRequestEntityId
import org.dataland.datalandaccountingservice.repositories.BilledRequestRepository
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.InheritedRole
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandcommunitymanager.openApiClient.api.InheritedRolesControllerApi
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.RequestSetToProcessingMessage
import org.dataland.datalandmessagequeueutils.messages.RequestSetToWithdrawnMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.data.repository.findByIdOrNull
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Listener class handling RabbitMQ messages sent to the accounting service.
 */
@Service("AccountingServiceListener")
class AccountingServiceListener(
    private val billedRequestRepository: BilledRequestRepository,
    private val inheritedRolesControllerApi: InheritedRolesControllerApi,
) {
    private val logger = LoggerFactory.getLogger(AccountingServiceListener::class.java)

    private fun getBilledCompanyId(triggeringUserId: String): String? {
        val inheritedRolesMap = inheritedRolesControllerApi.getInheritedRoles(triggeringUserId)
        return inheritedRolesMap.keys.firstOrNull {
            inheritedRolesMap[it]?.contains(InheritedRole.DatalandMember.name) ?: false
        }
    }

    private fun logBilledRequestMessage(
        requestSetToProcessingMessage: RequestSetToProcessingMessage,
        correlationId: String,
    ) = logger.info(
        "Received a message to create a billed request. Triggering user ID is ${requestSetToProcessingMessage.triggeringUserId} " +
            "and data sourcing ID is ${requestSetToProcessingMessage.dataSourcingId}. " +
            "Requested company ID is ${requestSetToProcessingMessage.requestedCompanyId}, " +
            "reporting period is ${requestSetToProcessingMessage.requestedReportingPeriod}, " +
            "and requested framework is ${requestSetToProcessingMessage.requestedFramework}. " +
            "Correlation ID: $correlationId.",
    )

    private fun logBilledRequestAbortionMessage(
        triggeringUserId: String,
        correlationId: String,
    ) = logger.info(
        "The user with ID $triggeringUserId is not a Dataland member. " +
            "Aborting the creation of an associated billed request object. Correlation ID: $correlationId.",
    )

    private fun logWithdrawRequestAbortionMessage(
        billableCompanyId: String,
        correlationId: String,
    ) = logger.info(
        "There is another billable request for the same company with ID $billableCompanyId. " +
            "Aborting the deletion of the associated billed request object. Correlation ID: $correlationId.",
    )

    private fun logDuplicateBilledRequestMessage(
        billedCompanyId: String,
        dataSourcingId: String,
        correlationId: String,
    ) = logger.info(
        "A billed request for company ID $billedCompanyId and data sourcing ID $dataSourcingId already exists. " +
            "Skipping creation. Correlation ID: $correlationId.",
    )

    private fun logDeleteBilledRequestMessage(
        billedCompanyId: String,
        dataSourcingId: String,
        correlationId: String,
    ) = logger.info(
        "The billed request for company ID $billedCompanyId and data sourcing ID $dataSourcingId was deleted. " +
            "Correlation ID: $correlationId.",
    )

    /**
     * Creates a billed request when a request is patched to the "Processing" state and the company to bill does not
     * already have a billed request for the given data sourcing ID.
     * @param payload the message payload
     * @param type the message type
     * @param correlationId the correlation ID of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.ACCOUNTING_SERVICE_REQUEST_PROCESSING,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.DATA_SOURCING_SERVICE_REQUEST_EVENTS),
                key = [RoutingKeyNames.REQUEST_PATCH],
            ),
        ],
    )
    fun createBilledRequestOnRequestPatchToStateProcessing(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.REQUEST_SET_TO_PROCESSING)
        val requestSetToProcessingMessage = MessageQueueUtils.readMessagePayload<RequestSetToProcessingMessage>(payload)
        if (requestSetToProcessingMessage.requestedFramework == DataTypeEnum.nuclearMinusAndMinusGas.value) {
            return
        }
        MessageQueueUtils.rejectMessageOnException {
            logBilledRequestMessage(requestSetToProcessingMessage, correlationId)

            val billedCompanyId = getBilledCompanyId(requestSetToProcessingMessage.triggeringUserId)
            if (billedCompanyId == null) {
                logBilledRequestAbortionMessage(requestSetToProcessingMessage.triggeringUserId, correlationId)
                return@rejectMessageOnException
            }
            saveBilledRequest(billedCompanyId, requestSetToProcessingMessage, correlationId)
        }
    }

    /** Saves a billed request to the database if one does not already exist for the given billed company ID
     * and data sourcing ID.
     */
    @Transactional
    fun saveBilledRequest(
        billedCompanyId: String,
        requestSetToProcessingMessage: RequestSetToProcessingMessage,
        correlationId: String,
    ) {
        val billedCompanyUUID = ValidationUtils.convertToUUID(billedCompanyId)
        val dataSourcingUUID = ValidationUtils.convertToUUID(requestSetToProcessingMessage.dataSourcingId)
        val requestedCompanyUUID = ValidationUtils.convertToUUID(requestSetToProcessingMessage.requestedCompanyId)

        val billedRequestEntityId =
            BilledRequestEntityId(
                billedCompanyId = billedCompanyUUID,
                dataSourcingId = dataSourcingUUID,
            )

        if (billedRequestRepository.findByIdOrNull(billedRequestEntityId) != null) {
            logDuplicateBilledRequestMessage(
                billedCompanyUUID.toString(),
                requestSetToProcessingMessage.dataSourcingId,
                correlationId,
            )
        } else {
            val billedRequestEntity =
                BilledRequestEntity(
                    billedCompanyId = billedCompanyUUID,
                    dataSourcingId = dataSourcingUUID,
                    requestedCompanyId = requestedCompanyUUID,
                    requestedReportingPeriod = requestSetToProcessingMessage.requestedReportingPeriod,
                    requestedFramework = requestSetToProcessingMessage.requestedFramework,
                    timestamp = Instant.now().toEpochMilli(),
                )
            billedRequestRepository.save(billedRequestEntity)
        }
    }

    /**
     * Deletes a billed request when a request is patched to the "Withdrawn" state, unless there is another
     * billable request for the same company.
     * @param payload the message payload
     * @param type the message type
     * @param correlationId the correlation ID of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.ACCOUNTING_SERVICE_REQUEST_WITHDRAWN,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.DATA_SOURCING_SERVICE_REQUEST_EVENTS),
                key = [RoutingKeyNames.REQUEST_WITHDRAWN],
            ),
        ],
    )
    fun deleteBilledRequestOnRequestPatchToWithdrawn(
        @Payload payload: String,
        @Header(MessageHeaderKey.TYPE) type: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.REQUEST_SET_TO_WITHDRAWN)
        val requestSetToWithdrawnMessage = MessageQueueUtils.readMessagePayload<RequestSetToWithdrawnMessage>(payload)
        MessageQueueUtils.rejectMessageOnException {
            val billedCompanyId = getBilledCompanyId(requestSetToWithdrawnMessage.triggeringUserId)
            if (billedCompanyId == null) {
                return@rejectMessageOnException
            }
            if (sameBillableRequestExistsForBilledCompany(requestSetToWithdrawnMessage, billedCompanyId)) {
                logWithdrawRequestAbortionMessage(billedCompanyId, correlationId)
                return@rejectMessageOnException
            }
            deleteBilledRequest(billedCompanyId, requestSetToWithdrawnMessage.dataSourcingId, correlationId)
        }
    }

    private fun sameBillableRequestExistsForBilledCompany(
        requestSetToWithdrawnMessage: RequestSetToWithdrawnMessage,
        billedCompanyId: String,
    ): Boolean = requestSetToWithdrawnMessage.userIdsAssociatedRequestsForSameTriple.any { getBilledCompanyId(it) == billedCompanyId }

    /** Deletes a billed request from the database for the given billed company ID and data sourcing ID.
     */
    @Transactional
    fun deleteBilledRequest(
        billedCompanyId: String,
        dataSourcingId: String,
        correlationId: String,
    ) {
        val billedCompanyUUID = ValidationUtils.convertToUUID(billedCompanyId)
        val dataSourcingUUID = ValidationUtils.convertToUUID(dataSourcingId)

        val billedRequestEntityId =
            BilledRequestEntityId(
                billedCompanyId = billedCompanyUUID,
                dataSourcingId = dataSourcingUUID,
            )
        val billedRequestEntity =
            billedRequestRepository.findByIdOrNull(billedRequestEntityId)
                ?: return
        logDeleteBilledRequestMessage(
            billedCompanyUUID.toString(),
            dataSourcingId,
            correlationId,
        )
        billedRequestRepository.delete(billedRequestEntity)
    }
}
