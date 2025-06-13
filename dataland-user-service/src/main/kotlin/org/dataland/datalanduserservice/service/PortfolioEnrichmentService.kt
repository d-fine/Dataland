package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolioEntry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service to manage Portfolio-related business logic
 */
@Service
class PortfolioEnrichmentService
    @Autowired
    constructor(
        private val metaDataControllerApi: MetaDataControllerApi,
        private val companyDataControllerApi: CompanyDataControllerApi,
    ) {
        private val majorFrameworks =
            listOf("sfdr", "eutaxonomy-financials", "eutaxonomy-non-financials", "nuclear-and-gas")

        /**
         * Construct an enriched entry using the passed company information, framework and latest reporting period
         */
        private fun getEnrichedEntry(
            companyInformation: BasicCompanyInformation,
            availableDataYearsStringPerFramework: Map<String, String?>,
        ) = EnrichedPortfolioEntry(
            companyId = companyInformation.companyId,
            companyName = companyInformation.companyName,
            sector = companyInformation.sector,
            countryCode = companyInformation.countryCode,
            "",
            mapOf(),
            availableDataYearsStringPerFramework,
        )

        /**
         * Return a mapping: (companyId) => ( mapping: (framework) => available reporting periods ) that has the
         * passed companyIds as keys, and the inner mapping has the passed frameworks as keys.
         * @param companyIds
         * @param frameworks
         */
        private fun getMapFromCompanyToMapFromFrameworkToAvailableReportingPeriodsSortedDescendingly(
            companyIds: List<String>,
            frameworks: List<String>,
        ): Map<String, Map<String, List<String>>> {
            val availableDataDimensions =
                metaDataControllerApi.getAvailableDataDimensions(
                    companyIds = companyIds,
                    frameworksOrDataPointTypes = frameworks,
                )

            val mapFromCompanyToListOfPairsOfFrameworkAndReportingPeriod =
                availableDataDimensions
                    .groupBy(
                        { it.companyId },
                        { Pair(it.dataType, it.reportingPeriod) },
                    )

            val mapFromCompanyToMapFromFrameworkToAvailableReportingPeriodsInAnyOrder =
                mapFromCompanyToListOfPairsOfFrameworkAndReportingPeriod.mapValues {
                    it.value.groupBy(
                        { it.first },
                        { it.second },
                    )
                }

            return mapFromCompanyToMapFromFrameworkToAvailableReportingPeriodsInAnyOrder.mapValues {
                it.value.mapValues {
                    it.value.sortedDescending()
                }
            }
        }

        /**
         * Return enriched portfolio entries for each passed company, using the passed frameworks as keys in the
         * mapping fields frameworkHyphenatedNamesToDataRef and availableReportingPeriods of each entry.
         * @param companyIdList the list of companies for which entries shall be returned
         * @param frameworkList the list of frameworks for which entries shall be returned
         */
        private fun getEnrichedEntries(
            companyIdList: List<String>,
            frameworkList: List<String>,
        ): List<EnrichedPortfolioEntry> {
            val companyValidationResults = companyDataControllerApi.postCompanyValidation(companyIdList)
            val mapFromCompanyToMapFromFrameworkToAvailableReportingPeriodsSortedDescendingly =
                getMapFromCompanyToMapFromFrameworkToAvailableReportingPeriodsSortedDescendingly(companyIdList, frameworkList)
            val enrichedEntries = mutableListOf<EnrichedPortfolioEntry>()

            companyValidationResults.forEach { validationResult ->
                val companyInformation = validationResult.companyInformation ?: return@forEach
                val mapFromFrameworkToAvailableReportingPeriodsSortedDescendingly =
                    mapFromCompanyToMapFromFrameworkToAvailableReportingPeriodsSortedDescendingly[companyInformation.companyId] ?: mapOf()
                enrichedEntries.add(
                    getEnrichedEntry(
                        companyInformation,
                        mapFromFrameworkToAvailableReportingPeriodsSortedDescendingly.mapValues {
                            it.value.fold("") { concatenationSoFar, nextReportingPeriod ->
                                if (concatenationSoFar.isEmpty()) {
                                    nextReportingPeriod
                                } else {
                                    "$concatenationSoFar, $nextReportingPeriod"
                                }
                            }
                        },
                    ),
                )
            }
            return enrichedEntries
        }

        /**
         * Retrieve an enriched portfolio for a given portfolio.
         */
        @Transactional(readOnly = true)
        fun getEnrichedPortfolio(portfolio: BasePortfolio): EnrichedPortfolio =
            EnrichedPortfolio(
                portfolioId = portfolio.portfolioId,
                portfolioName = portfolio.portfolioName,
                userId = portfolio.userId,
                entries =
                    getEnrichedEntries(
                        portfolio.companyIds.toList(),
                        majorFrameworks,
                    ),
                isMonitored = portfolio.isMonitored,
                startingMonitoringPeriod = portfolio.startingMonitoringPeriod,
                monitoredFrameworks = portfolio.monitoredFrameworks,
            )
    }
