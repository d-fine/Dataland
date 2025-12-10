package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalanduserservice.entity.NotificationEventEntity
import org.dataland.datalanduserservice.model.enums.NotificationFrequency
import org.dataland.datalanduserservice.repository.NotificationEventRepository
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Scheduler class for notifications sent out by the User Service.
 */
@Service
class NotificationScheduler
    @Autowired
    constructor(
        private val portfolioUpdateSummaryEmailBuilder: PortfolioUpdateSummaryEmailBuilder,
        private val portfolioRepository: PortfolioRepository,
        private val notificationEventRepository: NotificationEventRepository,
    ) {
        private val logger = LoggerFactory.getLogger(this.javaClass)

        companion object {
            private const val DAYS_IN_WEEK = 7L
            private const val MINUTES_FOR_TEST = 5L
        }

        /**
         * Processes portfolio update summary events and sends emails to appropriate recipients.
         * @param events List of unprocessed portfolio update summary notification events.
         */
        private fun processNotificationEvents(
            events: List<NotificationEventEntity>,
            frequency: NotificationFrequency,
            portfolioNamesString: String,
            userId: UUID,
        ) {
            if (events.isNotEmpty()) {
                logger.info(
                    "Requirements for Portfolio Update Summary notification are met." +
                        " Sending $frequency notification email to user $userId for portfolio(s) $portfolioNamesString.",
                )
                portfolioUpdateSummaryEmailBuilder.buildPortfolioMonitoringUpdateSummaryEmailAndSendCEMessage(
                    unprocessedEvents = events,
                    userId = userId,
                    frequency = frequency,
                    portfolioNamesString = portfolioNamesString,
                )
            } else {
                logger.info(
                    "No new events found for Portfolio Update Summary notification." +
                        " No $frequency email will be sent to user $userId for portfolio(s) $portfolioNamesString.",
                )
            }
        }

        /**
         * FOR TESTING PURPOSES ONLY
         * Scheduled method to send test summary emails.
         */
        @Scheduled(cron = "0 */5 * * * *")
        internal fun scheduledTestEmailSending() {
            val notificationFrequency = NotificationFrequency.Weekly
            val timeStampForInterval = Instant.now().minus(MINUTES_FOR_TEST, ChronoUnit.MINUTES).toEpochMilli()
            sendEmailForTimeInterval(notificationFrequency, timeStampForInterval)
        }

        /**
         * Scheduled method to send daily summary emails.
         * Runs every day at 7 am.
         */
        @Suppress("UnusedPrivateMember") // Detekt does not recognise the scheduled execution of this function
        @Scheduled(cron = "0 0 7 * * *", zone = "Europe/Berlin")
        internal fun scheduledDailyEmailSending() {
            val notificationFrequency = NotificationFrequency.Daily
            val timeStampForInterval = Instant.now().minus(1L, ChronoUnit.DAYS).toEpochMilli()
            sendEmailForTimeInterval(notificationFrequency, timeStampForInterval)
        }

        /**
         * Scheduled method to send weekly summary emails.
         * Runs every monday at 7 am.
         */
        @Suppress("UnusedPrivateMember") // Detekt does not recognise the scheduled execution of this function
        @Scheduled(cron = "0 0 7 * * MON", zone = "Europe/Berlin")
        internal fun scheduledWeeklyEmailSending() {
            val notificationFrequency = NotificationFrequency.Weekly
            val timeStampForInterval = Instant.now().minus(DAYS_IN_WEEK, ChronoUnit.DAYS).toEpochMilli()
            sendEmailForTimeInterval(notificationFrequency, timeStampForInterval)
        }

        /**
         * Scheduled method to send monthly summary emails.
         * Runs every day at 7 am on the first day of every month.
         */
        @Suppress("UnusedPrivateMember") // Detekt does not recognise the scheduled execution of this function
        @Scheduled(cron = "0 0 7 1 * *", zone = "Europe/Berlin")
        internal fun scheduledMonthlyEmailSending() {
            val notificationFrequency = NotificationFrequency.Monthly
            // Necessary since ChronoUnit.MONTHS is not supported for instant.now().minus()
            val timeStampForInterval =
                ZonedDateTime
                    .now(ZoneId.systemDefault())
                    .minusMonths(1)
                    .toInstant()
                    .toEpochMilli()
            sendEmailForTimeInterval(notificationFrequency, timeStampForInterval)
        }

        private fun sendEmailForTimeInterval(
            notificationFrequency: NotificationFrequency,
            timeStampForInterval: Long,
        ) {
            val portfoliosWithRegularUpdates =
                portfolioRepository.findAllByNotificationFrequencyAndIsMonitoredIsTrue(notificationFrequency)

            val portfoliosGroupedByUser = portfoliosWithRegularUpdates.groupBy { it.userId }

            portfoliosGroupedByUser.forEach { (userId, userPortfolios) ->
                val eventEntitiesToProcess =
                    userPortfolios.flatMap { it.monitoredFrameworks.orEmpty() }.toSet().flatMap { framework ->
                        val monitoredCompanies =
                            userPortfolios
                                .filter { it.monitoredFrameworks.orEmpty().contains(framework) }
                                .flatMap { it.companyIds }
                                .toSet()

                        mapPortfolioFrameworkToDataTypes(framework).flatMap { dataType ->
                            notificationEventRepository.findAllByFrameworkAndCompanyIdInAndCreationTimestampGreaterThan(
                                dataType,
                                monitoredCompanies.map { UUID.fromString(it) },
                                timeStampForInterval,
                            )
                        }
                    }
                processNotificationEvents(
                    events = eventEntitiesToProcess,
                    frequency = notificationFrequency,
                    portfolioNamesString = userPortfolios.joinToString(", ") { it.portfolioName },
                    userId = ValidationUtils.convertToUUID(userId),
                )
            }
        }

        private fun mapPortfolioFrameworkToDataTypes(framework: String): List<DataTypeEnum> {
            if (framework == "eutaxonomy") {
                return listOf(
                    DataTypeEnum.eutaxonomyMinusFinancials,
                    DataTypeEnum.eutaxonomyMinusNonMinusFinancials,
                    DataTypeEnum.nuclearMinusAndMinusGas,
                )
            }
            return listOf(DataTypeEnum.valueOf(framework))
        }
    }
