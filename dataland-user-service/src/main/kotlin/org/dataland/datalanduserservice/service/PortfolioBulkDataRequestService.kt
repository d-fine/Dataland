package org.dataland.datalanduserservice.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.BulkDataRequest
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.exceptions.BulkDataRequestJobFailedException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.PortfolioRequestFailure
import org.dataland.datalanduserservice.model.ReportingPeriodAndSector
import org.dataland.datalanduserservice.model.SectorType
import org.dataland.datalanduserservice.model.TimeWindowThreshold
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * Service to post Bulk Data Requests upon Portfolio or Monitoring status changes.
 * Currently restricted to Requests up to 2025. If required, change UPPER_BOUND accordingly.
 */
@Service
class PortfolioBulkDataRequestService
    @Autowired
    constructor(
        private val companyReportingInfoService: CompanyReportingInfoService,
        private val requestControllerApi: RequestControllerApi,
        val portfolioRepository: PortfolioRepository,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Schedules and executes the creation of Bulk Data Requests for all monitored portfolios in the system.
         *
         * This function runs automatically at 2:00 a.m. daily (server time).
         *  It retrieves all monitored portfolios, groups them by time window threshold,
         *  updates company reporting periods and sector information based on each threshold,
         *  and then publishes appropriate Bulk Data Requests for each portfolio.
         *
         * Failures are isolated: an error for one time window group or one portfolio is recorded and does not prevent
         * the remaining portfolios from being processed. Posting several requests for one portfolio is not atomic, so
         * requests posted before a failure remain; the next nightly run posts them again.
         * After all portfolios were attempted, an error summary is logged and a [BulkDataRequestJobFailedException]
         * is thrown if any failure occurred.
         *
         * @see postBulkDataRequest
         */
        @Suppress("UnusedPrivateMember") // Detekt does not recognize the scheduled execution of this function
        @Scheduled(cron = "0 */30 * * * *") // every 30 minutes for testing; change to "0 0 2 * * *" for production (2:00 a.m. daily))
        fun createBulkDataRequestsForAllMonitoredPortfolios() {
            logger.info("BulkDataRequest scheduled job started.")

            val allMonitoredPortfolios = portfolioRepository.findAllByIsMonitoredTrue()
            logger.info("Found ${allMonitoredPortfolios.size} monitored portfolios for processing.")
            companyReportingInfoService.resetData()

            val failures = mutableListOf<PortfolioRequestFailure>()
            allMonitoredPortfolios
                .groupBy { it.timeWindowThreshold }
                .forEach { (timeWindowThreshold, portfolios) ->
                    if (timeWindowThreshold != null && updateCompaniesSafely(timeWindowThreshold, portfolios, failures)) {
                        portfolios.forEach { postBulkDataRequestSafely(it, failures) }
                    }
                }

            if (failures.isNotEmpty()) {
                logger.error(
                    "BulkDataRequest scheduled job finished with ${failures.size} failure(s) among " +
                        "${allMonitoredPortfolios.size} portfolios: " +
                        failures.joinToString { "${it.portfolioId} (user ${it.userId}, ${it.stage}: ${it.cause.message})" },
                )
                throw BulkDataRequestJobFailedException(failures)
            }
            logger.info("BulkDataRequest scheduled job completed: processed ${allMonitoredPortfolios.size} portfolios.")
        }

        /**
         * Updates the company reporting info for all companies of the given portfolios.
         * If this fails, a failure is recorded for every portfolio of the group.
         *
         * @return true if the update succeeded and the portfolios can be processed, false otherwise
         */
        @Suppress("TooGenericExceptionCaught")
        private fun updateCompaniesSafely(
            threshold: TimeWindowThreshold,
            portfolios: List<PortfolioEntity>,
            failures: MutableList<PortfolioRequestFailure>,
        ): Boolean {
            val companyIds = portfolios.flatMap { it.companyIds }.toSet()
            logger.info(
                "Updating company reporting info for ${companyIds.size} unique company IDs" +
                    " across all portfolios with time window threshold $threshold.",
            )
            return try {
                companyReportingInfoService.updateCompanies(companyIds, threshold)
                logger.info("Company reporting info update completed.")
                true
            } catch (exception: Exception) {
                logger.error(
                    "Updating company reporting info failed for time window threshold $threshold. " +
                        "Skipping ${portfolios.size} portfolio(s).",
                    exception,
                )
                portfolios.forEach {
                    failures.add(it.toFailure(PortfolioRequestFailure.Stage.UPDATE_COMPANIES, exception))
                }
                false
            }
        }

        /**
         * Creates a [PortfolioRequestFailure] for the given portfolio, stage, and cause.
         * @param stage the processing stage in which the failure occurred
         * @param cause the exception that was thrown
         * @return a [PortfolioRequestFailure] instance representing the failure
         */
        private fun PortfolioEntity.toFailure(
            stage: PortfolioRequestFailure.Stage,
            cause: Exception,
        ) = PortfolioRequestFailure(portfolioId.toString(), userId, stage, cause)

        /**
         * Posts the bulk data requests for one portfolio. Failures are recorded and not propagated.
         */
        @Suppress("TooGenericExceptionCaught")
        private fun postBulkDataRequestSafely(
            portfolio: PortfolioEntity,
            failures: MutableList<PortfolioRequestFailure>,
        ) {
            try {
                postBulkDataRequest(portfolio.toBasePortfolio(), failures)
            } catch (e: Exception) {
                logger.error("Posting bulk data requests failed for portfolio ${portfolio.portfolioId} (user ${portfolio.userId}).", e)
                failures.add(portfolio.toFailure(PortfolioRequestFailure.Stage.POST_REQUESTS, e))
            }
        }

        /**
         * Publishes Bulk Data Request messages for a monitored portfolio.
         *
         * For each unique combination of sector and reporting period among the portfolio's company IDs,
         * posts one or more bulk data requests using the appropriate frameworks.
         * A failing request is logged with its context and recorded in [failures]; the remaining requests of the
         * portfolio are still posted.
         *
         * @param basePortfolio The BasePortfolio for which to publish bulk data requests.
         * @param failures The list to which a failure is added for each request that could not be posted.
         */
        private fun postBulkDataRequest(
            basePortfolio: BasePortfolio,
            failures: MutableList<PortfolioRequestFailure>,
        ) {
            val groupedCompanyIds = groupCompanyIdsBySectorAndReportingPeriod(basePortfolio.identifiers)
            var attempted = 0
            val failuresBefore = failures.size

            groupedCompanyIds.forEach { (key, companyIds) ->
                val companyIdSet = companyIds.toSet()
                if ("eutaxonomy" in basePortfolio.monitoredFrameworks) {
                    attempted++
                    postSafely(basePortfolio, "eutaxonomy", key, companyIdSet.size, failures) {
                        postEutaxonomyBulkRequest(basePortfolio, key, companyIdSet)
                    }
                }
                if ("sfdr" in basePortfolio.monitoredFrameworks) {
                    attempted++
                    postSafely(basePortfolio, "sfdr", key, companyIdSet.size, failures) {
                        postSfdrBulkRequest(basePortfolio, key.reportingPeriod, companyIdSet)
                    }
                }
            }
            logger.debug(
                "Posted ${attempted - (failures.size - failuresBefore)}/$attempted bulk data requests " +
                    "for portfolio ${basePortfolio.portfolioId} (user ${basePortfolio.userId}).",
            )
        }

        /**
         * Executes a single bulk request. A failure is logged with its context and recorded, not propagated.
         */
        @Suppress("TooGenericExceptionCaught", "LongParameterList")
        private fun postSafely(
            portfolio: BasePortfolio,
            framework: String,
            key: ReportingPeriodAndSector,
            companyCount: Int,
            failures: MutableList<PortfolioRequestFailure>,
            post: () -> Unit,
        ) {
            try {
                post()
            } catch (e: Exception) {
                logger.error(
                    "Posting $framework bulk data request failed for portfolio ${portfolio.portfolioId} " +
                        "(user ${portfolio.userId}): reportingPeriod=${key.reportingPeriod}, " +
                        "sector=${key.sector}, companies=$companyCount.",
                    e,
                )
                failures.add(
                    PortfolioRequestFailure(portfolio.portfolioId, portfolio.userId, PortfolioRequestFailure.Stage.POST_REQUESTS, e),
                )
            }
        }

        /**
         * Groups the provided companyIds by their sector and reporting period.
         *
         * Bulk Data Requests can then be posted for each group.
         * Only companyIds for which reporting and sector info is available are included.
         *
         * @param companyIds The set of company IDs to group.
         * @return A map from (reportingPeriod, sector) key to the set of companyIds sharing those attributes.
         */
        private fun groupCompanyIdsBySectorAndReportingPeriod(companyIds: Set<String>): Map<ReportingPeriodAndSector, List<String>> =
            companyReportingInfoService
                .getCachedReportingYearAndSectorInformation()
                .filterKeys { it in companyIds }
                .entries
                .flatMap { (companyId, reportingInfos) -> reportingInfos.map { info -> info to companyId } }
                .groupBy({ (info, _) -> info }, { (_, companyId) -> companyId })

        /**
         * Post a Bulk Data Request for the given portfolio, sector, and reporting period,
         * specifying the correct frameworks for 'eutaxonomy'.
         *
         * @param basePortfolio The portfolio for which to create the request.
         * @param groupKey The key specifying sector and reporting period.
         * @param companyIds The set of company IDs in this sector and reporting period group.
         */

        private fun postEutaxonomyBulkRequest(
            basePortfolio: BasePortfolio,
            groupKey: ReportingPeriodAndSector,
            companyIds: Set<String>,
        ) {
            val monitoredFrameworks =
                when (groupKey.sector) {
                    SectorType.FINANCIALS -> {
                        setOf(
                            DataTypeEnum.eutaxonomyMinusFinancialsMinus2026Minus73.value,
                        )
                    }

                    SectorType.NONFINANCIALS -> {
                        setOf(
                            DataTypeEnum.eutaxonomyMinusNonMinusFinancialsMinus2026Minus73.value,
                        )
                    }

                    else -> {
                        setOf(
                            DataTypeEnum.eutaxonomyMinusFinancialsMinus2026Minus73.value,
                            DataTypeEnum.eutaxonomyMinusNonMinusFinancialsMinus2026Minus73.value,
                        )
                    }
                }
            postBulkDataRequest(
                userId = basePortfolio.userId,
                companyIds = companyIds,
                reportingPeriods = setOf(groupKey.reportingPeriod),
                frameworks = monitoredFrameworks,
            )
        }

        /**
         * Post a bulk data request for the given portfolio and reporting period, specifying the 'sfdr' framework.
         *
         * @param basePortfolio The portfolio for which to create the request.
         * @param reportingPeriod The reporting period for which the request shall be posted.
         * @param companyIds The set of company IDs in this sector and reporting period group.
         */
        private fun postSfdrBulkRequest(
            basePortfolio: BasePortfolio,
            reportingPeriod: String,
            companyIds: Set<String>,
        ) {
            postBulkDataRequest(
                userId = basePortfolio.userId,
                companyIds = companyIds,
                reportingPeriods = setOf(reportingPeriod),
                frameworks = setOf(DataTypeEnum.sfdr.value),
            )
        }

        /**
         * Post a Bulk Data Request to the Data Sourcing Service.
         * @param userId: the id of the user to whom the portfolio belongs
         * @param companyIds: the company ids to be included in the request
         * @param reportingPeriods: the reporting periods
         * @param frameworks: the chosen frameworks
         */
        private fun postBulkDataRequest(
            userId: String,
            companyIds: Set<String>,
            reportingPeriods: Set<String>,
            frameworks: Set<String>,
        ) {
            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(
                    companyIdentifiers = companyIds,
                    dataTypes = frameworks,
                    reportingPeriods = reportingPeriods,
                ),
                userId = userId,
            )
            logger.info(
                "Bulk request posted for user $userId: frameworks=$frameworks, periods=$reportingPeriods " +
                    "and ${companyIds.size} unique company ids.",
            )
        }
    }
