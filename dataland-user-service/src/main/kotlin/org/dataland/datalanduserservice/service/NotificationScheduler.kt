package org.dataland.datalanduserservice.service

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalanduserservice.entity.NotificationEventEntity
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Scheduler class for notifications sent out by the User Service.
 */
@Service
class NotificationScheduler
    @Autowired
    constructor(
        private val dataRequestSummaryEmailBuilder: DataRequestSummaryEmailBuilder,
        private val portfolioRepository: PortfolioRepository,
        @PersistenceContext private val entityManager: EntityManager,
    ) {
        companion object {
            private const val DAYS_IN_A_WEEK = 7L
        }

        private val logger = LoggerFactory.getLogger(this.javaClass)

        /**
         * Processes data request summary events and sends emails to appropriate recipients.
         * @param events List of unprocessed data request summary notification events.
         */
        fun processNotificationEvents(
            events: List<NotificationEventEntity>,
            frequency: NotificationFrequency,
            portfolioNamesString: String,
            userId: UUID,
        ) {
            logger.info(
                "Requirements for Data Request Summary notification are met. Sending notification email to user $userId.",
            )
            dataRequestSummaryEmailBuilder.buildDataRequestSummaryEmailAndSendCEMessage(
                unprocessedEvents = events,
                userId = userId,
                frequency = frequency,
                portfolioNamesString = portfolioNamesString,
            )
        }

        /**
         * Scheduled method to send emails for unprocessed notification events.
         * Runs every Sunday at midnight.
         */
        @Scheduled(cron = "0 0 0 * * SUN")
        fun scheduledWeeklyEmailSending() {
            val notificationFrequency = NotificationFrequency.Weekly
            val oneWeekAgo = Instant.now().minus(DAYS_IN_A_WEEK, ChronoUnit.DAYS).toEpochMilli()
            val portfoliosWithWeeklyUpdates = portfolioRepository.findAllByNotificationFrequency(notificationFrequency)

            // Group portfolios by user
            val portfoliosGroupedByUser = portfoliosWithWeeklyUpdates.groupBy { it.userId }

            portfoliosGroupedByUser.forEach { (userId, userPortfolios) ->
                val allCompanyIdFrameworkPairs =
                    userPortfolios.flatMap { portfolio ->
                        val frameworks = portfolio.monitoredFrameworks ?: emptySet()
                        portfolio.companyIds.flatMap { companyId ->
                            frameworks.map { framework ->
                                // Adapt companyId to UUID if necessary
                                Pair(ValidationUtils.convertToUUID(companyId), DataTypeEnum.valueOf(framework))
                            }
                        }
                    }

                val eventEntitiesToProcess =
                    getRelevantNotificationEvents(
                        allCompanyIdFrameworkPairs,
                        oneWeekAgo,
                    )

                processNotificationEvents(
                    events = eventEntitiesToProcess,
                    frequency = notificationFrequency,
                    portfolioNamesString = userPortfolios.joinToString(", ") { it.portfolioName },
                    userId = ValidationUtils.convertToUUID(userId),
                )
            }
        }

        private fun getRelevantNotificationEvents(
            companyIdFrameworkPairs: List<Pair<UUID, DataTypeEnum>>,
            timeStamp: Long,
        ): List<NotificationEventEntity> {
            val formattedTuples =
                companyIdFrameworkPairs.joinToString(", ") {
                    "('${it.first}', '${it.second}')"
                }

            val queryToExecute =
                """SELECT * FROM notification_events n
                WHERE (n.company_id, n.framework) IN ($formattedTuples)
                AND n.creation_timestamp >= '$timeStamp'"""

            return if (companyIdFrameworkPairs.isNotEmpty()) {
                val query = entityManager.createNativeQuery(queryToExecute, NotificationEventEntity::class.java)
                return query.resultList
                    .filterIsInstance<NotificationEventEntity>()
            } else {
                emptyList()
            }
        }
    }
