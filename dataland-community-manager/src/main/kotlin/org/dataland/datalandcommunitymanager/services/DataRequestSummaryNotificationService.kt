package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestSummaryEmailSender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service that handles processing and sending (to email service) of investor relationship notification events.
 */
@Service("DataRequestSummaryNotificationService")
class DataRequestSummaryNotificationService
    @Autowired
    constructor(
        private val notificationEventRepository: NotificationEventRepository,
        private val dataRequestSummaryEmailSender: DataRequestSummaryEmailSender,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Processes data request summary events and sends emails to appropriate recipients.
         *
         * @param events List of unprocessed data request summary notification events.
         */
        fun processNotificationEvents(events: List<NotificationEventEntity>) {
            val eventsGroupedByUser = events.groupBy { it.userId }
            eventsGroupedByUser.forEach { (userId, userEvents) ->
                if (userId != null) {
                    logger.info(
                        "Requirements for Data Request Summary notification are met. Sending notification email.",
                    )
                    dataRequestSummaryEmailSender.sendDataRequestSummaryEmail(
                        unprocessedEvents = userEvents,
                        userId = userId,
                    )
                }
            }
        }

        /**
         * Creates a user-specific notification event in the "QA Status Accepted" and "Data Non-Sourceable" pipelines.
         * This function is also invoked by the "patch data request" endpoint of MetaDataController, but actions are
         * only performed in cases naturally covered by the pipelines.
         *
         * @param dataRequestEntity Represents the data request in question.
         * @param requestStatusAfter The request status after an update, if applicable.
         * @param immediateNotificationWasSent Boolean indicating if an immediate notification was already sent.
         * @param earlierQaApprovedVersionExists Boolean indicating if a prior QA approved version exists.
         */
        fun createUserSpecificNotificationEvent(
            dataRequestEntity: DataRequestEntity,
            requestStatusAfter: RequestStatus? = null,
            immediateNotificationWasSent: Boolean,
            earlierQaApprovedVersionExists: Boolean = false,
        ) {
            val notificationEventType =
                determineNotificationEventType(
                    requestStatusBefore = dataRequestEntity.requestStatus,
                    requestStatusAfter = requestStatusAfter,
                    earlierQaApprovedVersionExists = earlierQaApprovedVersionExists,
                )

            if (notificationEventType != null) {
                notificationEventRepository.save(
                    NotificationEventEntity(
                        notificationEventType = notificationEventType,
                        userId = UUID.fromString(dataRequestEntity.userId),
                        isProcessed = immediateNotificationWasSent,
                        companyId = UUID.fromString(dataRequestEntity.datalandCompanyId),
                        framework = DataTypeEnum.decode(dataRequestEntity.dataType)!!,
                        reportingPeriod = dataRequestEntity.reportingPeriod,
                    ),
                )
                emptyFunction()
            } else {
                logger.info("No valid event type found for notification creation.")
            }
        }

        private fun emptyFunction() {
        }

        /**
         * Determines the type of notification event based on the transition of request statuses
         * and whether an earlier QA-approved version exists.
         *
         * @param requestStatusBefore The request status before an update.
         * @param requestStatusAfter The request status after an update, if applicable.
         * @param earlierQaApprovedVersionExists Boolean indicating if a prior QA approved version exists.
         * @return The NotificationEventType corresponding to the request update status or null for unknown transitions
         */
        private fun determineNotificationEventType(
            requestStatusBefore: RequestStatus,
            requestStatusAfter: RequestStatus?,
            earlierQaApprovedVersionExists: Boolean,
        ): NotificationEventType? =
            when {
                // Case 1: Transition from Open or NonSourceable to Answered
                requestStatusBefore in listOf(RequestStatus.Open, RequestStatus.NonSourceable) &&
                    requestStatusAfter == RequestStatus.Answered -> {
                    NotificationEventType.UpdatedEvent.takeUnless { !earlierQaApprovedVersionExists }
                        ?: NotificationEventType.AvailableEvent
                }
                // Case 2: Status remains Answered, Closed,or Resolved
                requestStatusBefore in listOf(RequestStatus.Answered, RequestStatus.Closed, RequestStatus.Resolved) &&
                    requestStatusAfter in listOf(requestStatusBefore, null) -> {
                    NotificationEventType.UpdatedEvent
                }
                // Case 3: Transition from Open to NonSourceable
                requestStatusBefore == RequestStatus.Open && requestStatusAfter == RequestStatus.NonSourceable -> {
                    NotificationEventType.NonSourceableEvent
                }
                // Else: If none of the conditions match, this will be null
                else -> null
            }
    }
