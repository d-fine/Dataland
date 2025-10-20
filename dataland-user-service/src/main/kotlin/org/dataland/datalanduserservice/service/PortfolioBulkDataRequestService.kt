package org.dataland.datalanduserservice.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.BulkDataRequest
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service to post Bulk Data Requests upon Portfolio or Monitoring status changes.
 * Currently restricted to Requests up to 2025. If required, change UPPER_BOUND accordingly.
 */
@Service
class PortfolioBulkDataRequestService
    @Autowired
    constructor(
        private val portfolioEnrichmentService: PortfolioEnrichmentService,
        private val requestControllerApi: RequestControllerApi,
    ) {
        companion object {
            const val UPPER_BOUND = 2025
        }

        /**
         * Publishes Bulk Data Request Messages for monitored portfolios.
         */
        fun postBulkDataRequestMessageIfMonitored(portfolio: BasePortfolio) {
            if (!portfolio.isMonitored) {
                return
            }
            val enrichedPortfolio = portfolioEnrichmentService.getEnrichedPortfolio(portfolio)
            val monitoringPeriods = getMonitoringPeriods(portfolio.startingMonitoringPeriod)

            if ("eutaxonomy" in portfolio.monitoredFrameworks) {
                postFinancialBulkDataRequest(enrichedPortfolio, monitoringPeriods)
                postNonFinancialBulkDataRequest(enrichedPortfolio, monitoringPeriods)
                postFinancialAndNonFinancialBulkDataRequest(enrichedPortfolio, monitoringPeriods)
            }

            if ("sfdr" in portfolio.monitoredFrameworks) {
                postSfdrBulkDataRequest(enrichedPortfolio, monitoringPeriods)
            }
        }

        private fun filterCompanyIdsBySector(
            portfolio: EnrichedPortfolio,
            filterFunction: (String?) -> Boolean = { true },
        ): Set<String> =
            portfolio.entries
                .filter { filterFunction(it.sector) }
                .map { it.companyId }
                .toSet()

        private fun getFinancialsCompanyIds(portfolio: EnrichedPortfolio) =
            filterCompanyIdsBySector(portfolio) {
                it != null && it.lowercase() == "financials"
            }

        private fun getUnsectorizedCompanyIds(portfolio: EnrichedPortfolio) =
            filterCompanyIdsBySector(portfolio) {
                it == null
            }

        private fun getNonFinancialsCompanyIds(portfolio: EnrichedPortfolio) =
            filterCompanyIdsBySector(portfolio) { it != null && it.lowercase() != "financials" }

        private fun getAllCompanyIds(portfolio: EnrichedPortfolio) = filterCompanyIdsBySector(portfolio) { true }

        private fun getMonitoringPeriods(startingMonitoringPeriod: String?): Set<String> {
            val startingMonitoringPeriodInt =
                startingMonitoringPeriod?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid start year: '$startingMonitoringPeriod' is not a valid number")
            return (startingMonitoringPeriodInt until UPPER_BOUND)
                .map { it.toString() }
                .toSet()
        }

        /**
         * Post a Bulk Data Request based on the company sector
         * @param enrichedPortfolio: enrichment of the given portfolio
         * @param reportingPeriods: the monitoring periods
         * @param selector: the getter function for (non)-financial company Ids
         * @param monitoredFrameworks: the chosen frameworks
         */
        private fun postBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            reportingPeriods: Set<String>,
            selector: (EnrichedPortfolio) -> Set<String>,
            monitoredFrameworks: Set<String>,
        ) {
            val companyIds = selector(enrichedPortfolio)
            if (companyIds.isEmpty()) {
                return
            }

            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(
                    companyIdentifiers = companyIds,
                    dataTypes = monitoredFrameworks,
                    reportingPeriods = reportingPeriods,
                ),
                userId = DatalandAuthentication.fromContext().userId,
            )
        }

        /**
         * Post EU Taxonomy Bulk Data Requests for "financials" companies
         */
        private fun postFinancialBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = postBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getFinancialsCompanyIds,
            setOf(DataTypeEnum.eutaxonomyMinusFinancials.value, DataTypeEnum.nuclearMinusAndMinusGas.value),
        )

        /**
         * Post EU Taxonomy Bulk Data Requests for non-"financials" companies
         */
        private fun postNonFinancialBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = postBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getNonFinancialsCompanyIds,
            setOf(
                DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value,
                DataTypeEnum.nuclearMinusAndMinusGas.value,
            ),
        )

        /**
         * Post EU Taxonomy Bulk Data Requests for companies without sector
         */
        private fun postFinancialAndNonFinancialBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = postBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getUnsectorizedCompanyIds,
            setOf(
                DataTypeEnum.eutaxonomyMinusFinancials.value,
                DataTypeEnum.nuclearMinusAndMinusGas.value,
                DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value,
            ),
        )

        /**
         * Post SFDR Bulk Data Requests for all companies
         */

        private fun postSfdrBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = postBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getAllCompanyIds,
            setOf(
                DataTypeEnum.sfdr.value,
            ),
        )
    }
