package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.messaging.InvestorRelationshipsEmailBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service that handles creation, processing and sending of notification events,
 * when datasets are available and their company ownership is claimable.
 */
@Service("InvestorRelationshipNotificationService")
class InvestorRelationshipsNotificationService
    @Autowired
    constructor(
        private val notificationEventRepository: NotificationEventRepository,
        private val companyRolesManager: CompanyRolesManager,
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val investorRelationshipEmailBuilder: InvestorRelationshipsEmailBuilder,
    ) {
        private val logger = LoggerFactory.getLogger(this.javaClass)

        /**
         * Processes investor relationship events and sends emails to appropriate recipients.
         * @param notificationEvents List of unprocessed investor relationship notification events.
         */
        fun processNotificationEvents(notificationEvents: List<NotificationEventEntity>) {
            val eventsGroupedByCompany = notificationEvents.groupBy { it.companyId }
            eventsGroupedByCompany.forEach { (companyId, companyEvents) ->
                val companyInfo = companyDataControllerApi.getCompanyInfo(companyId.toString())
                val emailReceivers = companyInfo.companyContactDetails
                val correlationId = UUID.randomUUID().toString()

                if (!hasCompanyOwner(companyId) && !emailReceivers.isNullOrEmpty()) {
                    logger.info(
                        "Requirements for Investor Relationship notification are met. " +
                            "Sending notification emails. CorrelationId: $correlationId",
                    )
                    investorRelationshipEmailBuilder
                        .buildExternalAndInternalInvestorRelationshipsSummaryEmailAndSendCEMessage(
                            unprocessedEvents = companyEvents,
                            companyId = companyId,
                            receiver = emailReceivers,
                            correlationId = correlationId,
                        )
                }
            }
        }

        /**
         * Checks if a company has an owner assigned.
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
         * Creates a company-specific notification event in the "Investor Relationship Emails" pipeline.
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
