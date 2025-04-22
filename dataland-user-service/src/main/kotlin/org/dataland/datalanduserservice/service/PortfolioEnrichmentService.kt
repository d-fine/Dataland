package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolioEntry
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
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
        private val portfolioService: PortfolioService,
        private val metaDataControllerApi: MetaDataControllerApi,
        private val companyDataControllerApi: CompanyDataControllerApi,
    ) {
        private val logger = LoggerFactory.getLogger(PortfolioEnrichmentService::class.java)

        /**
         * Construct an enriched entry using the passed company information, framework and latest reporting period
         */
        private fun getEnrichedEntry(
            companyInformation: BasicCompanyInformation,
            framework: String,
            mostRecentDataYear: String?,
        ) = EnrichedPortfolioEntry(
            companyId = companyInformation.companyId,
            companyName = companyInformation.companyName,
            sector = companyInformation.sector,
            countryCode = companyInformation.countryCode,
            framework = framework,
            "",
            "",
            mostRecentDataYear,
        )

        /**
         * Return a mapping "(companyId, framework) --> latest available reporting period" for each combination of companies and frameworks
         * @param companyIds the list of companies for which the latest available reporting period shall be returned
         * @param dataTypes the list of frameworks for which the latest available reporting period shall be returned
         */
        private fun getMostRecentReportingPeriodPerCompanyAndFramework(
            companyIds: List<String>,
            dataTypes: List<String>,
        ): Map<Pair<String, String>, String> {
            val availableDataDimensions =
                metaDataControllerApi.getAvailableDataDimensions(
                    companyIds = companyIds,
                    dataTypes = dataTypes,
                )
            return availableDataDimensions.groupBy { Pair(it.companyId, it.dataType) }.mapValues {
                it.value.maxOf { it.reportingPeriod }
            }
        }

        /**
         * Return enriched portfolio entries for each combination of the passed companies and frameworks
         * @param companyIdList the list of companies for which entries shall be returned
         * @param frameworkList the list of frameworks for which entries shall be returned
         */
        private fun getEnrichedEntries(
            companyIdList: List<String>,
            frameworkList: List<String>,
        ): List<EnrichedPortfolioEntry> {
            val companyValidationResults = companyDataControllerApi.postCompanyValidation(companyIdList)
            val mostRecentData = getMostRecentReportingPeriodPerCompanyAndFramework(companyIdList, frameworkList)
            val enrichedEntries = mutableListOf<EnrichedPortfolioEntry>()

            companyValidationResults.forEach { validationResult ->
                val companyInformation = validationResult.companyInformation ?: return@forEach
                frameworkList.forEach { framework ->
                    enrichedEntries.add(
                        getEnrichedEntry(
                            companyInformation,
                            framework,
                            mostRecentData[Pair(companyInformation.companyId, framework)],
                        ),
                    )
                }
            }
            return enrichedEntries
        }

        /**
         * Retrieve an enriched portfolio for a given portfolio ID
         * @param portfolioId the portfolio identifier
         */
        @Transactional(readOnly = true)
        fun getEnrichedPortfolio(portfolioId: String): EnrichedPortfolio {
            val userId = DatalandAuthentication.fromContext().userId
            logger.info("Retrieve enriched portfolio with portfolioId: $portfolioId for user with userId: $userId.")

            val portfolio = portfolioService.getPortfolioForUser(portfolioId)
            return EnrichedPortfolio(
                portfolioId = portfolioId,
                portfolioName = portfolio.portfolioName,
                userId = portfolio.userId,
                entries =
                    getEnrichedEntries(
                        portfolio.companyIds.toList(),
                        portfolio.frameworks.map { it.value }.toList(),
                    ),
            )
        }
    }
