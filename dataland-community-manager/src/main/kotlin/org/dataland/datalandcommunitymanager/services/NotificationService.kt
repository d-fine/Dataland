package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.messaging.CompanyOwnershipClaimDatasetUploadedSender
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestSummaryEmailSender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service that handles creation of notification events and sending notifications to interested parties
 * in case of unprocessed notification events.
 */
@Service("NotificationService")
class NotificationService
    @Suppress("LongParameterList")
    @Autowired
    constructor(
        val notificationEventRepository: NotificationEventRepository,
        val companyRolesManager: CompanyRolesManager,
        val companyDataControllerApi: CompanyDataControllerApi,
        val notificationEmailSender: CompanyOwnershipClaimDatasetUploadedSender,
        val dataRequestSummaryEmailSender: DataRequestSummaryEmailSender,
    ) {
        private val logger = LoggerFactory.getLogger(this.javaClass)

        /**
         * Checks if there are unprocessed notification events.
         * If yes, sends Investor Relationship and/or Data Request Summary notification emails.
         * Scheduled to run every Sunday at midnight.
         */
        @Scheduled(cron = "0 0 0 ? * SUN")
        fun scheduledWeeklyEmailSending() {
            // Investor Relationship Emails
            val unprocessedInvestorRelationshipEvents =
                notificationEventRepository
                    .findAllByNotificationEventTypesAndIsProcessedFalse(
                        listOf(NotificationEventType.InvestorRelationshipsEvent),
                    )
            if (unprocessedInvestorRelationshipEvents.isNotEmpty()) {
                processInvestorRelationshipEvents(unprocessedInvestorRelationshipEvents)
                markEventsAsProcessed(unprocessedInvestorRelationshipEvents)
            }

            // Data Request Summary Emails
            val dataRequestSummaryEventTypes =
                listOf(
                    NotificationEventType.AvailableEvent,
                    NotificationEventType.UpdatedEvent,
                    NotificationEventType.NonSourceableEvent,
                )
            val unprocessedDataRequestSummaryEvents =
                notificationEventRepository
                    .findAllByNotificationEventTypesAndIsProcessedFalse(dataRequestSummaryEventTypes)
            if (unprocessedDataRequestSummaryEvents.isNotEmpty()) {
                processDataRequestSummaryEvents(unprocessedDataRequestSummaryEvents)
                markEventsAsProcessed(unprocessedDataRequestSummaryEvents)
            }
        }

        /**
         * Processes investor relationship events and sends emails to appropriate recipients.
         *
         * @param events List of unprocessed investor relationship notification events.
         */
        private fun processInvestorRelationshipEvents(events: List<NotificationEventEntity>) {
            // Group events by company ID and process each group
            val eventsGroupedByCompany = events.groupBy { it.companyId }
            eventsGroupedByCompany.forEach { (companyId, companyEvents) ->
                val companyInfo = companyDataControllerApi.getCompanyInfo(companyId.toString())
                val emailReceivers = companyInfo.companyContactDetails
                val correlationId = UUID.randomUUID().toString()

                // Send emails if company has no owner and has contact details
                if (!hasCompanyOwner(companyId) && !emailReceivers.isNullOrEmpty()) {
                    logger.info(
                        "Requirements for Investor Relationship notification are met. " +
                            "Sending notification emails. CorrelationId: $correlationId",
                    )
                    notificationEmailSender.sendExternalAndInternalInvestorRelationshipSummaryEmail(
                        unprocessedEvents = companyEvents,
                        companyId = companyId,
                        receiver = emailReceivers,
                        correlationId = correlationId,
                    )
                }
            }
        }

        /**
         * Processes data request summary events and sends emails to appropriate recipients.
         *
         * @param events List of unprocessed data request summary notification events.
         */
        private fun processDataRequestSummaryEvents(events: List<NotificationEventEntity>) {
            // Group events by user ID and process each group
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
         * Marks all given events as processed by setting isProcessed to true and saving changes to the repository.
         *
         * @param events List of notification events to mark as processed.
         */
        private fun markEventsAsProcessed(events: List<NotificationEventEntity>) {
            // Set each event's processed status to true and save them
            events.forEach { event ->
                event.isProcessed = true
            }
            notificationEventRepository.saveAll(events) // Batch save to update processed status
            logger.info("Marked ${events.size} events as processed.")
        }

        /**
         * Checks if a company has an owner assigned.
         *
         * @param companyId UUID representing the company's ID.
         * @return Boolean indicating whether the company has an owner.
         */
        private fun hasCompanyOwner(companyId: UUID): Boolean {
            val companyOwner =
                companyRolesManager.getCompanyRoleAssignmentsByParameters(
                    companyRole = CompanyRole.CompanyOwner,
                    companyId = companyId.toString(),
                    userId = null,
                )

            return companyOwner.isNotEmpty()
        }

        /**
         * Creates a user-specific notification event in the "QA Status Accepted" and "Data Non-Sourceable" pipelines.
         * This function is also invoked by the "patch data request" endpoint of MetaDataController, but actions are
         * only performed in cases naturally covered by the pipelines.
         *
         * @param dataRequestEntity Represents the data request in question.
         * @param requestStatusAfter The request status after an update, if applicable.
         * @param immediateNotificationWasSent Boolean indicating if an immediate notification was sent.
         * @param earlierQaApprovedVersionExists Boolean indicating if a prior QA approved version exists.
         */
        fun createUserSpecificNotificationEvent(
            dataRequestEntity: DataRequestEntity,
            requestStatusAfter: RequestStatus? = null,
            immediateNotificationWasSent: Boolean,
            earlierQaApprovedVersionExists: Boolean = false,
        ) {
            val requestStatusBefore = dataRequestEntity.requestStatus

            val requestStatusBeforeIsOpenOrNonSourceable =
                requestStatusBefore == RequestStatus.Open || requestStatusBefore == RequestStatus.NonSourceable

            if (requestStatusBeforeIsOpenOrNonSourceable && requestStatusAfter == RequestStatus.Answered) {
                notificationEventRepository.save(
                    NotificationEventEntity(
                        notificationEventType =
                            if (earlierQaApprovedVersionExists) {
                                NotificationEventType.UpdatedEvent
                            } else {
                                NotificationEventType.AvailableEvent
                            },
                        userId = UUID.fromString(dataRequestEntity.userId),
                        isProcessed = immediateNotificationWasSent,
                        companyId = UUID.fromString(dataRequestEntity.datalandCompanyId),
                        framework = DataTypeEnum.decode(dataRequestEntity.dataType)!!,
                        reportingPeriod = dataRequestEntity.reportingPeriod,
                    ),
                )
            }

            val requestStatusBeforeIsAnsweredOrClosedOrResolved =
                requestStatusBefore == RequestStatus.Answered ||
                    requestStatusBefore == RequestStatus.Closed ||
                    requestStatusBefore == RequestStatus.Resolved

            if (
                requestStatusBeforeIsAnsweredOrClosedOrResolved &&
                (requestStatusAfter == requestStatusBefore || requestStatusAfter == null)
            ) {
                notificationEventRepository.save(
                    NotificationEventEntity(
                        notificationEventType =
                            if (earlierQaApprovedVersionExists) {
                                NotificationEventType.UpdatedEvent
                            } else {
                                NotificationEventType.AvailableEvent
                            },
                        userId = UUID.fromString(dataRequestEntity.userId),
                        isProcessed = immediateNotificationWasSent,
                        companyId = UUID.fromString(dataRequestEntity.datalandCompanyId),
                        framework = DataTypeEnum.decode(dataRequestEntity.dataType)!!,
                        reportingPeriod = dataRequestEntity.reportingPeriod,
                    ),
                )
            }

            if (requestStatusBefore == RequestStatus.Open && requestStatusAfter == RequestStatus.NonSourceable) {
                notificationEventRepository.save(
                    NotificationEventEntity(
                        notificationEventType = NotificationEventType.NonSourceableEvent,
                        userId = UUID.fromString(dataRequestEntity.userId),
                        isProcessed = immediateNotificationWasSent,
                        companyId = UUID.fromString(dataRequestEntity.datalandCompanyId),
                        framework = DataTypeEnum.decode(dataRequestEntity.dataType)!!,
                        reportingPeriod = dataRequestEntity.reportingPeriod,
                    ),
                )
            }
        }

        /**
         * Creates a company-specific notification event in the "IR Emails" pipeline.
         *
         * @param dataMetaInformation Represents the metadata information for the company.
         */
        fun createCompanySpecificNotificationEvent(dataMetaInformation: DataMetaInformation) {
            notificationEventRepository.save(
                NotificationEventEntity(
                    notificationEventType = NotificationEventType.InvestorRelationshipsEvent,
                    userId = null,
                    isProcessed = false,
                    companyId = UUID.fromString(dataMetaInformation.companyId),
                    framework = dataMetaInformation.dataType,
                    reportingPeriod = dataMetaInformation.reportingPeriod,
                ),
            )
        }
    }
