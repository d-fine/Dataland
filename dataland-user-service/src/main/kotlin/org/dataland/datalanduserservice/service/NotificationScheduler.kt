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
import java.time.ZoneOffset
import java.time.ZonedDateTime
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
        private val logger = LoggerFactory.getLogger(this.javaClass)

        companion object {
            private const val DAYS_IN_WEEK = 7L
        }

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
            if (events.isNotEmpty()) {
                logger.info(
                    "Requirements for Data Request Summary notification are met." +
                        " Sending $frequency notification email to user $userId for portfolio(s) $portfolioNamesString.",
                )
                dataRequestSummaryEmailBuilder.buildDataRequestSummaryEmailAndSendCEMessage(
                    unprocessedEvents = events,
                    userId = userId,
                    frequency = frequency,
                    portfolioNamesString = portfolioNamesString,
                )
            } else {
                logger.info(
                    "No new events found for Data Request Summary notification." +
                        " No $frequency email will be sent to user $userId for portfolio(s) $portfolioNamesString.",
                )
            }
        }

        /**
         * FOR TESTING PURPOSES ONLY
         * Scheduled method to send test summary emails.
         */
        @Scheduled(cron = "0 1/10 * * * *")
        fun scheduledTestEmailSending() {
            val notificationFrequency = NotificationFrequency.Weekly
            val timeStampForInteval = Instant.now().minus(DAYS_IN_WEEK, ChronoUnit.DAYS).toEpochMilli()
            sendEmailForTimeInterval(notificationFrequency, timeStampForInteval)
        }

        /**
         * Scheduled method to send weekly summary emails.
         * Runs every monday at 7 am.
         */
        @Scheduled(cron = "0 0 7 * * MON")
        fun scheduledWeeklyEmailSending() {
            val notificationFrequency = NotificationFrequency.Weekly
            val timeStampForInterval = Instant.now().minus(DAYS_IN_WEEK, ChronoUnit.DAYS).toEpochMilli()
            sendEmailForTimeInterval(notificationFrequency, timeStampForInterval)
        }

        /**
         * Scheduled method to send daily summary emails.
         * Runs every day at 7 am.
         */
        @Scheduled(cron = "0 0 7 * * *")
        fun scheduledDailyEmailSending() {
            val notificationFrequency = NotificationFrequency.Daily
            val timeStampForInterval = Instant.now().minus(1L, ChronoUnit.DAYS).toEpochMilli()
            sendEmailForTimeInterval(notificationFrequency, timeStampForInterval)
        }

        /**
         * Scheduled method to send monthly summary emails.
         * Runs every day at 7 am on the first day of every month.
         */
        @Scheduled(cron = "0 0 7 1 * *")
        fun scheduledMonthlyEmailSending() {
            val notificationFrequency = NotificationFrequency.Monthly
            val timeStampForInterval =
                ZonedDateTime
                    .now(ZoneOffset.UTC)
                    .minusMonths(1)
                    .toInstant()
                    .toEpochMilli()
            sendEmailForTimeInterval(notificationFrequency, timeStampForInterval)
        }

        private fun sendEmailForTimeInterval(
            notificationFrequency: NotificationFrequency,
            timeStampForInterval: Long,
        ) {
            logger.info("Sending notifications for $notificationFrequency")
            val portfoliosWithRegularUpdates =
                portfolioRepository.findAllByNotificationFrequencyAndIsMonitoredIsTrue(notificationFrequency)
            logger.info("Found ${portfoliosWithRegularUpdates.size} portfolios with regular updates.")

            val portfoliosGroupedByUser = portfoliosWithRegularUpdates.groupBy { it.userId }
            logger.info("Found ${portfoliosGroupedByUser.size} unique users with regular updates.")

            portfoliosGroupedByUser.forEach { (userId, userPortfolios) ->
                val allCompanyIdFrameworkPairs =
                    userPortfolios.flatMap { portfolio ->
                        val frameworks =
                            (portfolio.monitoredFrameworks ?: emptySet()).flatMap { framework ->
                                when (framework) {
                                    "eutaxonomy" ->
                                        listOf(
                                            "eutaxonomy-financials",
                                            "eutaxonomy-non-financials",
                                            "nuclear-and-gas",
                                        )

                                    else -> listOf(framework)
                                }
                            }
                        portfolio.companyIds.flatMap { companyId ->
                            frameworks.mapNotNull { framework ->
                                DataTypeEnum
                                    .decode(framework)
                                    ?.let { Pair(ValidationUtils.convertToUUID(companyId), it) }
                            }
                        }
                    }

                val eventEntitiesToProcess =
                    getRelevantNotificationEvents(
                        allCompanyIdFrameworkPairs,
                        timeStampForInterval,
                    )
                logger.info("Found ${eventEntitiesToProcess.size} relevant events for user $userId")
                logger.info("The events are: ${eventEntitiesToProcess.joinToString(",")}")

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
                logger.info("Executing query: $queryToExecute")
                return query.resultList
                    .filterIsInstance<NotificationEventEntity>()
            } else {
                emptyList()
            }
        }
    }
