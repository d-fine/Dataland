package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.repository.PortfolioRepository
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdentifierValidationResult
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolioEntry

/**
 * Service to manage Portfolio-related business logic
 */
@Service
class PortfolioEnrichmentService
    @Autowired
    constructor(
        private val portfolioRepository: PortfolioRepository,
        private val portfolioService: PortfolioService,
        private val metaDataControllerApi: MetaDataControllerApi,
        private val companyDataControllerApi: CompanyDataControllerApi
    ) {
        private val logger = LoggerFactory.getLogger(PortfolioEnrichmentService::class.java)

        fun getEnrichedPortfolio(portfolioId: String): EnrichedPortfolio {
            val portfolio = portfolioService.getPortfolioForUser(portfolioId)
            val companyIds = portfolio.companyIds.toList()
            val frameworks = portfolio.frameworks.map { it.value }.toList()

            val entries = listOf<EnrichedPortfolioEntry>()
            val companyValidationResults = companyDataControllerApi.postCompanyValidation(portfolio.companyIds.toList())
            val allAvailableDataDimensions = metaDataControllerApi.getAvailableData(companyIds = companyIds, dataTypes = frameworks)
            val mostCurrentData = getMostCurrentData(allAvailableDataDimensions)
            buildEnrichedPortfolioEntry()




            return EnrichedPortfolio(portfolio)
        }

        fun getMostCurrentData(dimensions: List<BasicDataDimensions>): Map<String, List<BasicDataDimensions>> {
            val result = mutableMapOf<String, List<BasicDataDimensions>>()
            val allCompanyIds = dimensions.map { it.companyId }.distinct()
            val allFrameworks = dimensions.map { it.dataType }.distinct()
            for (companyId in allCompanyIds) {
                val highestReportingPeriods = mutableListOf<BasicDataDimensions>()
                for (framework in allFrameworks) {
                    highestReportingPeriods.add(
                        dimensions.filter { it.companyId == companyId && it.dataType == framework }
                        .maxBy { it.reportingPeriod })
                }
                result[companyId] = highestReportingPeriods
                }
            return result
            }


    fun buildEnrichedPortfolioEntry(companyValidationResults: List<CompanyIdentifierValidationResult>): EnrichedPortfolioEntry {



    }
        }



