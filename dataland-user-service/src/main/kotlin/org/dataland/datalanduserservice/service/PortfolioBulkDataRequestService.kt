package org.dataland.datalanduserservice.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.BulkDataRequest
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanduserservice.entity.PortfolioEntity
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate

// This is due to some weird detekt error
const val THRESHOLD2 = 3L

/**
 * Service to post Bulk Data Requests upon Portfolio or Monitoring status changes.
 * Currently restricted to Requests up to 2025. If required, change UPPER_BOUND accordingly.
 */
@Service
class PortfolioBulkDataRequestService
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val requestControllerApi: RequestControllerApi,
        val portfolioRepository: PortfolioRepository,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        internal var threshold1InMonths = 1L
        internal var threshold2InMonths = THRESHOLD2

        private val financialsString = "financials"
        private val nonfinancialsString = "nonfinancials"
        private val unsectorizedString = "unsectorized"

        var companyIdsWithoutReportingYearInfo: MutableSet<String> = mutableSetOf()

        private var setOfCompanyReportingYearAndSectorInfo: MutableSet<CompanyReportingYearAndSectorInfo> = mutableSetOf()

        internal data class CompanyReportingYearAndSectorInfo(
            val companyId: String,
            val reportingPeriod: String,
            val sector: String?,
        )

        /**
         * Schedules and executes the creation of Bulk Data Requests for all monitored portfolios in the system.
         *
         * This function runs automatically at 2:00 a.m. daily (server time).
         * It retrieves all monitored portfolios, updates company reporting year and sector information,
         * and then publishes appropriate Bulk Data Requests for each portfolio.
         *
         * @see setCompanyReportingYearAndSectorInfo
         * @see postBulkDataRequestIfMonitored
         */
        @Suppress("UnusedPrivateMember") // Detect does not recognize the scheduled execution of this function
        @Scheduled(cron = "0 0 2 * * *")
        private fun createBulkDataRequestsForAllMonitoredPortfolios() {
            logger.info("Running scheduled request creation job for monitored portfolios.")
            val allMonitoredPortfolios = portfolioRepository.findAllByIsMonitoredTrue()
            setCompanyReportingYearAndSectorInfo(allMonitoredPortfolios)
            allMonitoredPortfolios.forEach { postBulkDataRequestIfMonitored(it.toBasePortfolio()) }
        }

        /**
         * Sets the reporting year and sector information for all company IDs across the provided monitored portfolios.
         *
         * Iterates over all company IDs in the monitored portfolios, retrieves their reporting info,
         * and updates the setOfCompanyReportingYearAndSectorInfo and companyIdsWithoutReportingYearInfo accordingly.
         * Skips company IDs that have already been processed or are known to have missing reporting info.
         *
         * @param allMonitoredPortfolios List of all monitored PortfolioEntity items to collect company info from.
         */
        private fun setCompanyReportingYearAndSectorInfo(allMonitoredPortfolios: List<PortfolioEntity>) {
            setOfCompanyReportingYearAndSectorInfo = mutableSetOf()
            companyIdsWithoutReportingYearInfo = mutableSetOf()

            val processedCompanyIds = mutableSetOf<String>()
            allMonitoredPortfolios
                .flatMap { it.companyIds }
                .toSet()
                .forEach { id ->
                    if (!processedCompanyIds.contains(id) && !companyIdsWithoutReportingYearInfo.contains(id)) {
                        processedCompanyIds += id
                        val storedCompany = companyDataControllerApi.getCompanyById(id)
                        getCompanyReportingYearInfoForCompany(storedCompany)
                            ?.let { setOfCompanyReportingYearAndSectorInfo += it }
                    }
                }
        }

        /**
         * Retrieves the reporting year and sector information for a specific company.
         *
         * Calculates the relevant reporting year based on the company's fiscal year end and reporting period shift.
         * Sectors are normalized to "financials", "nonfinancials", or "unsectorized".
         * If required information is unavailable, the company is added to companyIdsWithoutReportingYearInfo and null is returned.
         *
         * @param storedCompany The StoredCompany object containing company information.
         * @return CompanyReportingYearAndSectorInfo for the company, or null if insufficient data.
         */
        internal fun getCompanyReportingYearInfoForCompany(storedCompany: StoredCompany): CompanyReportingYearAndSectorInfo? {
            val (fiscalYearEnd, reportingPeriodShift, normalizedSector) =
                with(storedCompany.companyInformation) {
                    Triple(
                        fiscalYearEnd,
                        reportingPeriodShift,
                        when (sector?.lowercase()) {
                            financialsString -> financialsString
                            null -> unsectorizedString
                            else -> nonfinancialsString
                        },
                    )
                }

            if (fiscalYearEnd == null || reportingPeriodShift == null) {
                companyIdsWithoutReportingYearInfo += storedCompany.companyId
                return null
            }

            val today = LocalDate.now()
            val lowerBoundary = today.minusMonths(threshold1InMonths)
            val upperBoundary = today.plusMonths(threshold2InMonths)

            val candidatesForRelevantFYE =
                listOf(
                    fiscalYearEnd.withYear(lowerBoundary.year),
                    fiscalYearEnd.withYear(upperBoundary.year),
                )

            val selectedRelevantFYE =
                candidatesForRelevantFYE
                    .firstOrNull { it.isAfter(lowerBoundary) && it.isBefore(upperBoundary) }

            val reportingYear = selectedRelevantFYE?.plusYears(reportingPeriodShift.toLong())?.year

            return if (reportingYear != null) {
                CompanyReportingYearAndSectorInfo(
                    storedCompany.companyId,
                    reportingYear.toString(),
                    normalizedSector,
                )
            } else {
                null
            }
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

            val reportingPeriodAndSectorInfoByCompanyId = buildCompanyInfoLookup()
            val groupedCompanyIds =
                groupCompanyIdsBySectorAndReportingPeriod(
                    basePortfolio.companyIds,
                    reportingPeriodAndSectorInfoByCompanyId,
                )

            groupedCompanyIds.forEach { (key, companyIds) ->
                if ("eutaxonomy" in basePortfolio.monitoredFrameworks) {
                    postEutaxonomyBulkRequest(basePortfolio, key, companyIds)
                }
                if ("sfdr" in basePortfolio.monitoredFrameworks) {
                    postSfdrBulkRequest(basePortfolio, key, companyIds)
                }
            }
        }

        private fun buildCompanyInfoLookup(): Map<String, CompanyReportingYearAndSectorInfo> =
            setOfCompanyReportingYearAndSectorInfo.associateBy { it.companyId }

        private data class ReportingGroupKey(
            val sector: String,
            val reportingPeriod: String,
        )

        /**
         * Groups the provided companyIds by their sector and reporting period. Bulk Data Requests can then be posted
         * for each group.
         * Only companyIds for which reporting and sector info is available are included.
         * @param companyIds The set of company IDs to group.
         * @param infoLookup A map from companyId to CompanyReportingYearAndSectorInfo.
         * @return A map from (sector, reportingPeriod) key to the set of companyIds sharing those attributes.
         */
        private fun groupCompanyIdsBySectorAndReportingPeriod(
            companyIds: Set<String>,
            infoLookup: Map<String, CompanyReportingYearAndSectorInfo>,
        ): Map<ReportingGroupKey, Set<String>> =
            companyIds
                .mapNotNull { companyId ->
                    infoLookup[companyId]?.let { info ->
                        ReportingGroupKey(
                            sector = info.sector?.lowercase() ?: unsectorizedString,
                            reportingPeriod = info.reportingPeriod,
                        ) to companyId
                    }
                }.groupBy({ it.first }, { it.second })
                .mapValues { it.value.toSet() }

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
            groupKey: ReportingGroupKey,
            companyIds: Set<String>,
        ) {
            val monitoredFrameworks =
                when (groupKey.sector) {
                    financialsString ->
                        setOf(
                            DataTypeEnum.eutaxonomyMinusFinancials.value,
                            DataTypeEnum.nuclearMinusAndMinusGas.value,
                        )

                    nonfinancialsString ->
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
         * Post a Bulk Data Request for the given portfolio, sector, and reporting period,
         * specifying the 'sfdr' framework.
         *
         * @param basePortfolio The portfolio for which to create the request.
         * @param groupKey The key specifying sector and reporting period.
         * @param companyIds The set of company IDs in this sector and reporting period group.
         */
        private fun postSfdrBulkRequest(
            basePortfolio: BasePortfolio,
            groupKey: ReportingGroupKey,
            companyIds: Set<String>,
        ) {
            postBulkDataRequest(
                userId = basePortfolio.userId,
                companyIds = companyIds,
                reportingPeriods = setOf(groupKey.reportingPeriod),
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
        }
    }
