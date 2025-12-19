package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanduserservice.model.ReportingPeriodAndSector
import org.dataland.datalanduserservice.model.SectorType
import org.dataland.datalanduserservice.model.TimeWindowThreshold
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

typealias CompanyId = String

/**
 * Service to fetch, process, and cache reporting year and sector information for companies.
 *
 * This service interacts with the Company Data API to retrieve company information,
 * calculates the relevant reporting year based on fiscal year-end and reporting period shift.
 * It maintains a cache of this data for efficient access.
 */
@Service
class CompanyReportingInfoService
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
    ) {
        companion object {
            private const val STARTING_OF_SOURCING_WINDOW_THRESHOLD_1_IN_MONTHS = 6L
            private const val STARTING_OF_EXTENDED_SOURCING_WINDOW_THRESHOLD_1_IN_MONTHS = 16L
            private const val END_OF_SOURCING_WINDOW_THRESHOLD_2_IN_MONTHS = 1L
        }

        private val companyIdsWithoutReportingYearInfo: MutableSet<CompanyId> = mutableSetOf()
        private val reportingYearsAndSectors: MutableMap<CompanyId, List<ReportingPeriodAndSector>> = mutableMapOf()

        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Resets the cached data.
         */
        fun resetData() {
            companyIdsWithoutReportingYearInfo.clear()
            reportingYearsAndSectors.clear()
        }

        /**
         * Update the cache by fetching, processing, and storing reporting year and
         * sector information for the given company IDs based on the specified time window threshold.
         *
         * @param companyIds A collection of company IDs to update or add.
         * @param timeWindowThreshold The time window threshold for portfolio monitoring.
         */
        fun updateCompanies(
            companyIds: Collection<CompanyId>,
            timeWindowThreshold: TimeWindowThreshold,
        ) {
            companyIds.distinct().forEach { companyId ->
                val storedCompany = companyDataControllerApi.getCompanyById(companyId)
                val infos = getCompanyReportingYearInfosForCompany(storedCompany, timeWindowThreshold)

                if (infos.isNotEmpty()) {
                    reportingYearsAndSectors[companyId] = infos
                }

                if ((storedCompany.companyInformation.fiscalYearEnd == null) ||
                    (storedCompany.companyInformation.reportingPeriodShift == null)
                ) {
                    companyIdsWithoutReportingYearInfo.add(companyId)
                }
            }
            logger.info(
                "Found ${reportingYearsAndSectors.values.sumOf { it.size }} reporting periods " +
                    "across ${reportingYearsAndSectors.size} companies with reporting year info, " +
                    "and ${companyIdsWithoutReportingYearInfo.size} companies without reporting year info.",
            )
        }

        /**
         * Returns the cached map of company IDs to their corresponding list of reporting periods and sector information.
         *
         * This map is populated by calling the `updateCompanies` method with a list of company IDs.
         * If a company's reporting year or sector information is unavailable, it will not be included in the map.
         */
        fun getCachedReportingYearAndSectorInformation(): Map<CompanyId, List<ReportingPeriodAndSector>> = reportingYearsAndSectors.toMap()

        /**
         * Returns the cached set of company IDs that lack reporting year information.
         *
         * This set is populated by calling the `updateCompanies` method with a list of company IDs.
         * Companies without sufficient data to determine their reporting year are added to this set.
         */
        fun getCachedCompanyIdsWithoutReportingYearInfo(): Set<CompanyId> = companyIdsWithoutReportingYearInfo.toSet()

        /**
         * Retrieves the reporting year and sector information for a specific company.
         *
         * Calculates the relevant reporting year based on the company's fiscal year-end, reporting period shift and time window threshold.
         * Sectors are normalized to "financials", "nonfinancials", or "unknown".
         * If required information is unavailable, this function returns null.
         *
         * @param storedCompany The StoredCompany object containing company information.
         * @param timeWindowThreshold The time window threshold for portfolio monitoring.
         * @return ReportingPeriodAndSector for the company, or null if insufficient data.
         */
        private fun getCompanyReportingYearInfosForCompany(
            storedCompany: StoredCompany,
            timeWindowThreshold: TimeWindowThreshold,
        ): List<ReportingPeriodAndSector> {
            val storedFiscalYearEnd = storedCompany.companyInformation.fiscalYearEnd
            val reportingPeriodShift = storedCompany.companyInformation.reportingPeriodShift

            if (storedFiscalYearEnd == null || reportingPeriodShift == null) return emptyList()

            val reportingYears = resolveReportingYears(storedFiscalYearEnd, reportingPeriodShift, timeWindowThreshold)
            val sector = resolveSectorType(storedCompany.companyInformation.sector)

            return reportingYears.map { year ->
                ReportingPeriodAndSector(
                    reportingPeriod = year.toString(),
                    sector = sector,
                )
            }
        }

        private fun resolveReportingYears(
            fiscalYearEnd: LocalDate,
            reportingPeriodShift: Int,
            timeWindowThreshold: TimeWindowThreshold,
        ): List<Int> {
            val today = LocalDate.now()
            val lowerBoundary =
                today.minusMonths(
                    if (timeWindowThreshold == TimeWindowThreshold.Extended) {
                        STARTING_OF_EXTENDED_SOURCING_WINDOW_THRESHOLD_1_IN_MONTHS
                    } else {
                        STARTING_OF_SOURCING_WINDOW_THRESHOLD_1_IN_MONTHS
                    },
                )
            val upperBoundary = today.minusMonths(END_OF_SOURCING_WINDOW_THRESHOLD_2_IN_MONTHS)

            val candidateDates =
                (lowerBoundary.year..upperBoundary.year)
                    .asSequence()
                    .map { year -> fiscalYearEnd.withYear(year) }
                    .filter { candidate ->
                        candidate.isAfter(lowerBoundary) && candidate.isBefore(upperBoundary)
                    }.toList()

            return candidateDates
                .map { candidate -> candidate.plusYears(reportingPeriodShift.toLong()).year }
        }

        private fun resolveSectorType(sector: String?): SectorType =
            when (sector?.lowercase()) {
                null -> SectorType.UNKNOWN
                SectorType.FINANCIALS.sectorName -> SectorType.FINANCIALS
                else -> SectorType.NONFINANCIALS
            }
    }
