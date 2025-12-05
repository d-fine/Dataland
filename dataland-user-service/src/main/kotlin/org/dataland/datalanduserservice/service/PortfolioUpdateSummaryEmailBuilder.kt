package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.COMPANY_NOT_FOUND
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.dataland.datalandmessagequeueutils.messages.email.EmailRecipient
import org.dataland.datalandmessagequeueutils.messages.email.PortfolioMonitoringUpdateSummaryEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.dataland.datalanduserservice.entity.NotificationEventEntity
import org.dataland.datalanduserservice.model.enums.NotificationEventType
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.utils.readableFrameworkNameMapping
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * A service used to build scheduled monitored portfolio updates summary emails,
 * containing updates about data of monitored portfolios of one user
 */
@Service("PortfolioUpdateSummaryEmailBuilder")
class PortfolioUpdateSummaryEmailBuilder
    @Autowired
    constructor(
        private val cloudEventMessageHandler: CloudEventMessageHandler,
        private val companyApi: CompanyDataControllerApi,
    ) {
        private val objectMapper = defaultObjectMapper

        private fun buildExceptionMessageDueToCompanyNotFound(companyId: String) = "Dataland does not know the company ID $companyId"

        /**
         * Builds the Portfolio Monitoring Update Summary email and sends CE message.
         * @param unprocessedEvents A list of notification event entities that are unprocessed
         * and contained in the summary email.
         * @param userId The ID of the user to whom the email should be sent.
         */
        fun buildPortfolioMonitoringUpdateSummaryEmailAndSendCEMessage(
            unprocessedEvents: List<NotificationEventEntity>,
            userId: UUID,
            frequency: NotificationFrequency,
            portfolioNamesString: String,
        ) {
            val emailContent = portfolioMonitoringUpdateSummaryEmailContent(unprocessedEvents, frequency, portfolioNamesString)
            val receiver = listOf(EmailRecipient.UserId(userId.toString()))
            val message = EmailMessage(emailContent, receiver, emptyList(), emptyList())
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                objectMapper.writeValueAsString(message),
                MessageType.SEND_EMAIL,
                correlationId = UUID.randomUUID().toString(),
                ExchangeName.SEND_EMAIL,
                RoutingKeyNames.EMAIL,
            )
        }

        /**
         * Constructs the content of the Portfolio Monitoring Update Summary email from the provided events.
         * @param events A list of notification event entities to process.
         * @return The email content that encapsulates the summary of updates of monitored portfolios.
         */
        private fun portfolioMonitoringUpdateSummaryEmailContent(
            events: List<NotificationEventEntity>,
            frequency: NotificationFrequency,
            portfolioName: String,
        ): TypedEmailContent {
            val newData = aggregateFrameworkDataForOneEventType(events, NotificationEventType.AvailableEvent)
            val updatedData = aggregateFrameworkDataForOneEventType(events, NotificationEventType.UpdatedEvent)
            val nonSourceableData =
                aggregateFrameworkDataForOneEventType(events, NotificationEventType.NonSourceableEvent)
            return PortfolioMonitoringUpdateSummaryEmailContent(
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
        ): List<PortfolioMonitoringUpdateSummaryEmailContent.FrameworkData> {
            val filteredEventTypeEvents = events.filter { it.notificationEventType == eventType }
            val groupedEvents = filteredEventTypeEvents.groupBy { Pair(it.framework, it.reportingPeriod) }

            return groupedEvents.map { (key, group) ->
                val (dataType, reportingPeriod) = key
                val companies = group.map { getValidCompanyNameOrId(it.companyId.toString()) }.distinct()
                PortfolioMonitoringUpdateSummaryEmailContent.FrameworkData(
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
                        COMPANY_NOT_FOUND,
                        buildExceptionMessageDueToCompanyNotFound(companyId),
                    )
                } else {
                    throw e
                }
            }
        }
    }
