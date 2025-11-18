package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestSummaryEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.dataland.datalanduserservice.entity.NotificationEventEntity
import org.dataland.datalanduserservice.model.enums.NotificationEventType
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.utils.readableFrameworkNameMapping
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * A service used to build scheduled data request summary emails, containing all data requests updates for one user
 */
@Service("DataRequestSummaryEmailBuilder")
class DataRequestSummaryEmailBuilder
    @Autowired
    constructor(
        private val cloudEventMessageHandler: CloudEventMessageHandler,
        private val companyApi: CompanyDataControllerApi,
    ) {
        private val logger = LoggerFactory.getLogger(this.javaClass)
        private val objectMapper = defaultObjectMapper
        private val exceptionSummaryDueToCompanyNotFound = "Company not found"

        private fun buildExceptionMessageDueToCompanyNotFound(companyId: String) = "Dataland does not know the company ID $companyId"

        /**
         * Builds the Data Requests Summary email and sends CE message.
         * @param unprocessedEvents A list of notification event entities that are unprocessed
         * and contained in the summary email.
         * @param userId The ID of the user to whom the email should be sent.
         */
        fun buildDataRequestSummaryEmailAndSendCEMessage(
            unprocessedEvents: List<NotificationEventEntity>,
            userId: UUID,
            frequency: NotificationFrequency,
            portfolioNamesString: String,
        ) {
            logger.info("Building Data Request Summary email for user with userId: $userId.")
            logger.info("Data Request Summary email will contain the following events: $unprocessedEvents.")
            logger.info("Data Request Summary email will be sent at frequency: $frequency.")
            logger.info("Data Request Summary email will be sent for portfolio(s): $portfolioNamesString.")
            val emailContent = dataRequestSummaryEmailContent(unprocessedEvents, frequency, portfolioNamesString)
            logger.info("Data Request Summary email content: $emailContent.")
            val receiver = listOf(EmailRecipient.UserId(userId.toString()))
            logger.info("Data Request Summary email receiver: $receiver.")
            val message = EmailMessage(emailContent, receiver, emptyList(), emptyList())
            logger.info("Data Request Summary CE message: $message.")
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(message),
                MessageType.SEND_EMAIL,
                correlationId = UUID.randomUUID().toString(),
                ExchangeName.SEND_EMAIL,
                RoutingKeyNames.EMAIL,
            )
        }

        /**
         * Constructs the content of the Data Request Summary email from the provided events.
         * @param events A list of notification event entities to process.
         * @return The email content that encapsulates the summary of data requests.
         */
        private fun dataRequestSummaryEmailContent(
            events: List<NotificationEventEntity>,
            frequency: NotificationFrequency,
            portfolioName: String,
        ): TypedEmailContent {
            val newData = aggregateFrameworkDataForOneEventType(events, NotificationEventType.AvailableEvent)
            val updatedData = aggregateFrameworkDataForOneEventType(events, NotificationEventType.UpdatedEvent)
            val nonSourceableData =
                aggregateFrameworkDataForOneEventType(events, NotificationEventType.NonSourceableEvent)
            return DataRequestSummaryEmailContent(
                newData,
                updatedData,
                nonSourceableData,
                frequency.toString(),
                portfolioName,
            )
        }

        /**
         * Aggregates framework data for a specific event type.
         * @param events A list of notification event entities to process.
         * @param eventType The type of notification event to filter and aggregate data for.
         * @return A list of aggregated framework data objects.
         */
        private fun aggregateFrameworkDataForOneEventType(
            events: List<NotificationEventEntity>,
            eventType: NotificationEventType,
        ): List<DataRequestSummaryEmailContent.FrameworkData> {
            val filteredEventTypeEvents = events.filter { it.notificationEventType == eventType }
            val groupedEvents = filteredEventTypeEvents.groupBy { Pair(it.framework, it.reportingPeriod) }

            return groupedEvents.map { (key, group) ->
                val (dataType, reportingPeriod) = key
                val companies = group.map { getValidCompanyNameOrId(it.companyId.toString()) }.distinct()
                DataRequestSummaryEmailContent.FrameworkData(
                    dataTypeLabel = readableFrameworkNameMapping[dataType] ?: dataType.toString(),
                    reportingPeriod = reportingPeriod,
                    companies = companies,
                )
            }
        }

        /**
         * Checks if a companyId exists on Dataland by trying to retrieve it in the backend.
         * If it does not exist the method catches the not-found-exception from the backend and throws a
         * resource-not-found exception here in the community manager.
         * @param companyId is the companyId to check for
         * @returns the name of the company if available, otherwise the companyId
         */
        fun getValidCompanyNameOrId(companyId: String): String {
            try {
                return companyApi
                    .getCompanyById(companyId)
                    .companyInformation.companyName
                    .ifEmpty { companyId }
            } catch (e: ClientException) {
                if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
                    throw ResourceNotFoundApiException(
                        exceptionSummaryDueToCompanyNotFound,
                        buildExceptionMessageDueToCompanyNotFound(companyId),
                    )
                } else {
                    throw e
                }
            }
        }
    }
