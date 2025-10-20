package org.dataland.datalanduserservice.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.BulkDataRequest
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.ReportingPeriodAndSector
import org.dataland.datalanduserservice.model.SectorType
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
         * Creates Bulk Data Requests for a specific portfolio.
         *
         * This function is called when a portfolio is created or its monitoring status changes,
         * or when a company is added to the portfolio.
         * It updates the company reporting year and sector information for the companies in the portfolio,
         * and then publishes appropriate Bulk Data Requests if the portfolio is monitored.
         *
         * @param basePortfolio The BasePortfolio for which to create bulk data requests.
         */
        fun createBulkDataRequestsForPortfolioIfMonitored(basePortfolio: BasePortfolio) {
            logger.info(
                "Updating company reporting info for ${basePortfolio.companyIds.size} unique company ids for" +
                    " portfolio with id ${basePortfolio.portfolioId} of user ${basePortfolio.userId}.",
            )
            companyReportingInfoService.updateCompanies(basePortfolio.companyIds)
            postBulkDataRequestIfMonitored(basePortfolio)
        }

        /**
         * Schedules and executes the creation of Bulk Data Requests for all monitored portfolios in the system.
         *
         * This function runs automatically at 2:00 a.m. daily (server time).
         * It retrieves all monitored portfolios, updates company reporting year and sector information,
         * and then publishes appropriate Bulk Data Requests for each portfolio.
         *
         * @see postBulkDataRequestIfMonitored
         */
        @Suppress("UnusedPrivateMember") // Detect does not recognize the scheduled execution of this function
        @Scheduled(cron = "0 0 2 * * *")
        private fun createBulkDataRequestsForAllMonitoredPortfolios() {
            logger.info("BulkDataRequest scheduled job started.")

            val allMonitoredPortfolios = portfolioRepository.findAllByIsMonitoredTrue()
            logger.info("Found ${allMonitoredPortfolios.size} monitored portfolios for processing.")

            companyReportingInfoService.resetData()
            val allCompanyIds = allMonitoredPortfolios.flatMap { it.companyIds }.toSet()
            logger.info("Updating company reporting info for ${allCompanyIds.size} unique company IDs across portfolios.")
            companyReportingInfoService.updateCompanies(allCompanyIds)
            logger.info("Company reporting info update completed.")
            allMonitoredPortfolios.forEach {
                postBulkDataRequestIfMonitored(it.toBasePortfolio())
            }

            logger.info("BulkDataRequest scheduled job completed: processed ${allMonitoredPortfolios.size} portfolios.")
        }

        /**
         * Publishes Bulk Data Request messages for a monitored portfolio.
         *
         * For each unique combination of sector and reporting period among the portfolio's company IDs,
         * posts one or more bulk data requests using the appropriate frameworks.
         * Does nothing if the provided portfolio is not monitored.
         *
         * @param basePortfolio The BasePortfolio for which to publish bulk data requests.
         */
        fun postBulkDataRequestIfMonitored(basePortfolio: BasePortfolio) {
            if (!basePortfolio.isMonitored) return

            val groupedCompanyIds = groupCompanyIdsBySectorAndReportingPeriod(basePortfolio.companyIds)

            groupedCompanyIds.forEach { (key, companyIds) ->
                if ("eutaxonomy" in basePortfolio.monitoredFrameworks) {
                    postEutaxonomyBulkRequest(basePortfolio, key, companyIds.toSet())
                }
                if ("sfdr" in basePortfolio.monitoredFrameworks) {
                    postSfdrBulkRequest(basePortfolio, key.reportingPeriod, companyIds.toSet())
                }
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
                .filter {
                    it.key in companyIds
                }.entries
                .groupBy({ it.value }, { it.key })

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
                    SectorType.FINANCIALS ->
                        setOf(
                            DataTypeEnum.eutaxonomyMinusFinancials.value,
                            DataTypeEnum.nuclearMinusAndMinusGas.value,
                        )

                    SectorType.NONFINANCIALS ->
                        setOf(
                            DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value,
                            DataTypeEnum.nuclearMinusAndMinusGas.value,
                        )

                    else ->
                        setOf(
                            DataTypeEnum.eutaxonomyMinusFinancials.value,
                            DataTypeEnum.nuclearMinusAndMinusGas.value,
                            DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value,
                        )
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
         * Post a Bulk Data Request based on the company sector
         * @param userId: the id of the user to whom the portfolio belongs
         * @param companyIds: the company ids to be included in the request
         * @param reportingPeriods: the monitoring periods
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
