package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.SourceabilityInfo
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.MessageWithTriple
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.messages.SourceabilityMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalanduserservice.entity.NotificationEventEntity
import org.dataland.datalanduserservice.model.enums.NotificationEventType
import org.dataland.datalanduserservice.repository.NotificationEventRepository
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

/**
 * A service used to listen for notification events and process them accordingly
 */
@Service("NotificationEventListener")
class NotificationEventListener
    @Autowired
    constructor(
        private val notificationEventRepository: NotificationEventRepository,
    ) {
        private val logger = LoggerFactory.getLogger(this.javaClass)

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
                            QueueNames.ACCOUNTING_SERVICE_NON_SOURCABLE_EVENT,
                            arguments = [
                                Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                                Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                                Argument(name = "defaultRequeueRejected", value = "false"),
                            ],
                        ),
                    exchange = Exchange(ExchangeName.DATASOURCING_DATA_NONSOURCEABLE, declare = "false"),
                    key = [RoutingKeyNames.DATASOURCING_NONSOURCEABLE],
                ),
            ],
        )
        fun processMessageForDataReportedAsNonSourceable(
            @Payload payload: String,
            @Header(MessageHeaderKey.TYPE) type: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        ) {
            MessageQueueUtils.validateMessageType(type, MessageType.DATASOURCING_NONSOURCEABLE)
            val sourceabilityMessage = MessageQueueUtils.readMessagePayload<SourceabilityMessage>(payload)

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

            val notificationEventEntity =
                NotificationEventEntity(
                    companyId = ValidationUtils.convertToUUID(sourceabilityMessage.companyId),
                    framework = sourceabilityInfo.dataType,
                    reportingPeriod = sourceabilityInfo.reportingPeriod,
                    notificationEventType = NotificationEventType.NonSourceableEvent,
                )
            notificationEventRepository.save(notificationEventEntity)
        }

        /**
         * Listens for messages that specify data as available or updated
         * and creates corresponding notification events in the database.
         * @param payload the message describing the result of the data availability or update event
         * @param type the type of the message
         * @param correlationId the correlation id of the message
         */
        @RabbitListener(
            bindings = [
                QueueBinding(
                    value =
                        Queue(
                            QueueNames.ACCOUNTING_SERVICE_QA_STATUS_UPDATE_EVENT,
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
        fun processMessageForAvailableDataAndUpdates(
            @Payload payload: String,
            @Header(MessageHeaderKey.TYPE) type: String,
            @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        ) {
            MessageQueueUtils.validateMessageType(type, MessageType.QA_STATUS_UPDATED)
            val qaStatusChangeMessage = MessageQueueUtils.readMessagePayload<QaStatusChangeMessage>(payload)

            checkThatReceivedDataIsComplete(qaStatusChangeMessage)
            val dataTypeDecoded = decodeDataTypeIfPossible(qaStatusChangeMessage)

            logger.info(
                "Received a qa status change message for data type: ${qaStatusChangeMessage.dataType}, " +
                    "company ID: ${qaStatusChangeMessage.companyId} and reporting period: " +
                    "${qaStatusChangeMessage.reportingPeriod} with isUpdate: ${qaStatusChangeMessage.isUpdate}. " +
                    "Correlation ID: $correlationId",
            )

            val notificationEventEntity =
                NotificationEventEntity(
                    companyId = ValidationUtils.convertToUUID(qaStatusChangeMessage.companyId),
                    framework = dataTypeDecoded,
                    reportingPeriod = qaStatusChangeMessage.reportingPeriod,
                    notificationEventType =
                        if (qaStatusChangeMessage.isUpdate) {
                            NotificationEventType.UpdatedEvent
                        } else {
                            NotificationEventType.AvailableEvent
                        },
                )
            notificationEventRepository.save(notificationEventEntity)
        }

        /**
         * Checks whether at least one of the fields companyId or reportingPeriod in the message
         * is empty and, if so, throws an appropriate exception.
         */
        private fun checkThatReceivedDataIsComplete(messageWithTriple: MessageWithTriple) {
            if (messageWithTriple.companyId.isEmpty() || messageWithTriple.reportingPeriod.isEmpty()) {
                throw MessageQueueRejectException("Both companyId and reportingPeriod must be provided.")
            }
        }

        /**
         * Checks whether the message actually corresponds to a dataset being set to non-sourceable
         * (as opposed to it being set to sourceable). If not, it throws an appropriate exception.
         */
        private fun checkThatDatasetWasSetToNonSourceable(sourceabilityMessage: SourceabilityMessage) {
            if (!sourceabilityMessage.isNonSourceable) {
                throw MessageQueueRejectException(
                    "Received event did not set a data " +
                        "sourcing object to status non-sourceable.",
                )
            }
        }

        /**
         * Tries to decode the dataType string field in the message to a DataTypeEnum object to return.
         * If the decoding fails, an appropriate exception is thrown.
         */
        private fun decodeDataTypeIfPossible(messageWithTriple: MessageWithTriple): DataTypeEnum =
            DataTypeEnum.decode(messageWithTriple.dataType)
                ?: throw MessageQueueRejectException("Framework name could not be understood.")
    }
