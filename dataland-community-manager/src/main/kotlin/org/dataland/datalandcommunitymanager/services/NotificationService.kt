package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service that handles creation of notification events and sending notifications to interested parties
 * in case of elementary events
 */
@Service("NotificationService")
class NotificationService
    @Suppress("LongParameterList")
    @Autowired
    constructor(
        val notificationEventRepository: NotificationEventRepository,
        val companyRolesManager: CompanyRolesManager,
        /*val companyDataControllerApi: CompanyDataControllerApi,
        val notificationEmailSender: NotificationEmailSender,*/
    ) {
        // private val logger = LoggerFactory.getLogger(this.javaClass)

        /*
        /**
         * Checks if there are unprocessed notification events.
         * If yes, sends Investor Relationship and/or Data Request Summary notification emails.
         */
        @Scheduled(cron = "0 0 0 ? * SUN")
        fun scheduledWeeklyEmailSending() {
            // Investor Relationship Emails
            val unprocessedInvestorRelationshipEvents =
                notificationEventRepository
                    .findAllByNotificationEventTypeAndIsProcessedFalse(
                        notificationEventType = NotificationEventType.InvestorRelationshipsEvent,
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
                    .findAllByUserIdAndNotificationEventTypeInAndIsProcessedFalse(dataRequestSummaryEventTypes)
            if (unprocessedDataRequestSummaryEvents.isNotEmpty()) {
                processDataRequestSummaryEvents(unprocessedDataRequestSummaryEvents)
                markEventsAsProcessed(unprocessedDataRequestSummaryEvents)
            }
        }

        /**
         * Processes investor relationship events and sends emails to appropriate recipients.
         */
        private fun processInvestorRelationshipEvents(events: List<NotificationEventEntity>) {
            val eventsGroupedByCompany = events.groupBy { it.companyId }
            eventsGroupedByCompany.forEach { (companyId, companyEvents) ->
                val companyInfo = companyDataControllerApi.getCompanyInfo(companyId.toString())
                val emailReceivers = companyInfo.companyContactDetails
                val correlationId = UUID.randomUUID().toString() // toto: generate oder get

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
         */
        private fun processDataRequestSummaryEvents(events: List<NotificationEventEntity>) {
//            toto
            if (events.isEmpty()) return // line added so events is used and detekt does not complain
            notificationEmailSender.sendDataRequestSummaryEmail() // toto
        }

        /**
         * Marks all given events as processed by setting isProcessed to true.
         */
        private fun markEventsAsProcessed(events: List<NotificationEventEntity>) {
            events.forEach { event ->
                event.isProcessed = true
            }
            notificationEventRepository.saveAll(events) // Batch save to update processed status
            logger.info("Marked ${events.size} events as processed.")
        }

        /**
         * checks if company has owner (if company has owner, notifications are created but not sent)
         */
        fun hasCompanyOwner(companyId: UUID): Boolean {
            val companyOwner =
                companyRolesManager.getCompanyRoleAssignmentsByParameters(
                    companyRole = CompanyRole.CompanyOwner,
                    companyId = companyId.toString(),
                    userId = null,
                )

            return companyOwner.isNotEmpty()
        }
         */

        /**
         * Create the suitable user-specific notification event in the "QA Status Accepted" and "Data Non-Sourceable" pipelines.
         * Do note that this function is also called by the "patch data request" endpoint of MetaDataController, but it will
         * only do something in the cases that are naturally covered by the pipelines.
         * @param dataRequestEntity represents the data request in question
         */
        fun createUserSpecificNotificationEvent(
            dataRequestEntity: DataRequestEntity,
            requestStatusAfter: RequestStatus? = null,
            immediateNotificationWasSent: Boolean,
            earlierQaApprovedVersionExists: Boolean = false,
        ) {
            val requestStatusBefore = dataRequestEntity.getLatestRequestStatus()

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
         * Create the suitable company-specific notification event in the "IR Emails" pipeline.
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
