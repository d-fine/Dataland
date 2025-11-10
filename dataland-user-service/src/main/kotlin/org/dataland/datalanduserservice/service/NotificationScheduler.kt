package org.dataland.datalanduserservice.service

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
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
            portfolioName: String,
        ) {
            val eventsGroupedByUser = events.groupBy { it.userId }
            eventsGroupedByUser.forEach { (userId, userEvents) ->
                logger.info(
                    "Requirements for Data Request Summary notification are met. Sending notification email.",
                )
                dataRequestSummaryEmailBuilder.buildDataRequestSummaryEmailAndSendCEMessage(
                    unprocessedEvents = userEvents,
                    userId = userId,
                    frequency = frequency,
                    portfolioName = portfolioName,
                )
            }
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

            portfoliosWithWeeklyUpdates.forEach { portfolio ->
                val frameworks = portfolio.monitoredFrameworks ?: emptySet()
                val companyIdFrameworkPairs: List<Pair<UUID, DataTypeEnum>> =
                    portfolio.companyIds.flatMap { companyId ->
                        frameworks.map { framework ->
                            Pair(UUID.fromString(companyId), DataTypeEnum.valueOf(framework))
                        }
                    }

                val eventEntitiesToProcess =
                    getRelevantNotificationEvents(
                        companyIdFrameworkPairs,
                        oneWeekAgo,
                    )

                processNotificationEvents(
                    eventEntitiesToProcess,
                    notificationFrequency,
                    portfolio.portfolioName, // pass the portfolio name
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
