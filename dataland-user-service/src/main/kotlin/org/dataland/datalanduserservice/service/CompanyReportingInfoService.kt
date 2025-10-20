package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalanduserservice.model.ReportingPeriodAndSector
import org.dataland.datalanduserservice.model.SectorType
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
            private const val THRESHOLD_1_IN_MONTHS = 1L
            private const val THRESHOLD_2_IN_MONTHS = 3L
        }

        private val companyIdsWithoutReportingYearInfo: MutableSet<CompanyId> = mutableSetOf()
        private val reportingYearsAndSectors: MutableMap<CompanyId, ReportingPeriodAndSector> = mutableMapOf()

        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Resets the cached data.
         */
        fun resetData() {
            companyIdsWithoutReportingYearInfo.clear()
            reportingYearsAndSectors.clear()
        }

        /**
         * Update the cache by fetching, processing, and storing reporting year and sector information for the given company IDs.
         *
         * @param companyIds A collection of company IDs to update or add.
         */
        fun updateCompanies(companyIds: Collection<CompanyId>) {
            companyIds.distinct().forEach { companyId ->
                val storedCompany = companyDataControllerApi.getCompanyById(companyId)
                getCompanyReportingYearInfoForCompany(storedCompany)?.let {
                    reportingYearsAndSectors[companyId] = it
                } ?: companyIdsWithoutReportingYearInfo.add(companyId)
            }
            logger.info(
                "Found ${reportingYearsAndSectors.size} companies with reporting year info," +
                    " and ${companyIdsWithoutReportingYearInfo.size} companies without reporting year info.",
            )
        }

        /**
         * Returns the cached map of company IDs to their corresponding reporting year and sector information.
         *
         * This map is populated by calling the `updateCompanies` method with a list of company IDs.
         * If a company's reporting year or sector information is unavailable, it will not be included in the map.
         */
        fun getCachedReportingYearAndSectorInformation(): MutableMap<CompanyId, ReportingPeriodAndSector> = reportingYearsAndSectors

        /**
         * Returns the cached set of company IDs that lack reporting year information.
         *
         * This set is populated by calling the `updateCompanies` method with a list of company IDs.
         * Companies without sufficient data to determine their reporting year are added to this set.
         */
        fun getCachedCompanyIdsWithoutReportingYearInfo(): Set<CompanyId> = companyIdsWithoutReportingYearInfo

        /**
         * Retrieves the reporting year and sector information for a specific company.
         *
         * Calculates the relevant reporting year based on the company's fiscal year-end and reporting period shift.
         * Sectors are normalized to "financials", "nonfinancials", or "unknown".
         * If required information is unavailable, this function returns null.
         *
         * @param storedCompany The StoredCompany object containing company information.
         * @return ReportingPeriodAndSector for the company, or null if insufficient data.
         */
        private fun getCompanyReportingYearInfoForCompany(storedCompany: StoredCompany): ReportingPeriodAndSector? {
            val storedFiscalYearEnd = storedCompany.companyInformation.fiscalYearEnd
            val reportingPeriodShift = storedCompany.companyInformation.reportingPeriodShift

            if (storedFiscalYearEnd != null && reportingPeriodShift != null) {
                val today = LocalDate.now()
                val lowerBoundary = today.minusMonths(THRESHOLD_1_IN_MONTHS)
                val upperBoundary = today.plusMonths(THRESHOLD_2_IN_MONTHS)

                val fiscalYearEnd =
                    listOf(
                        storedFiscalYearEnd.withYear(lowerBoundary.year),
                        storedFiscalYearEnd.withYear(upperBoundary.year),
                    ).firstOrNull {
                        it.isAfter(lowerBoundary) && it.isBefore(upperBoundary)
                    }

                val reportingYear = fiscalYearEnd?.plusYears(reportingPeriodShift.toLong())?.year

                return if (reportingYear == null) {
                    null
                } else {
                    ReportingPeriodAndSector(
                        reportingPeriod = reportingYear.toString(),
                        sector =
                            when (storedCompany.companyInformation.sector?.lowercase()) {
                                null -> SectorType.UNKNOWN
                                SectorType.FINANCIALS.sectorName -> SectorType.FINANCIALS
                                else -> SectorType.NONFINANCIALS
                            },
                    )
                }
            } else {
                return null
            }
        }
    }
