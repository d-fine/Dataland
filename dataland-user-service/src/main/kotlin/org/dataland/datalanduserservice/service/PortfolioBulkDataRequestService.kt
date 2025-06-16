package org.dataland.datalanduserservice.service

import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.BulkDataRequest
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service to create Bulk Data Requests upon Portfolio or Monitoring changes.
 * Currently restricted to Requests up to 2024. If required, change UPPER_BOUND accordingly.
 */
@Service
class PortfolioBulkDataRequestService
    @Autowired
    constructor(
        private val requestControllerApi: RequestControllerApi,
        private val portfolioEnrichmentService: PortfolioEnrichmentService,
    ) {
        companion object {
            private const val UPPER_BOUND = 2025
        }

        /**
         * Sends Bulk Data Requests with respect to the Monitoring Status and Company Sectors.
         */
        fun sendBulkDataRequestIfMonitored(portfolio: BasePortfolio) {
            if (!portfolio.isMonitored) {
                return
            }
            val enrichedPortfolio = portfolioEnrichmentService.getEnrichedPortfolio(portfolio)
            val monitoringPeriods = getMonitoringPeriods(portfolio.startingMonitoringPeriod)

            if ("eutaxonomy" in portfolio.monitoredFrameworks) {
                sendFinancialBulkDataRequest(enrichedPortfolio, monitoringPeriods)
                sendNonFinancialBulkDataRequest(enrichedPortfolio, monitoringPeriods)
                sendFinancialAndNonFinancialBulkDataRequest(enrichedPortfolio, monitoringPeriods)
            }

            if ("sfdr" in portfolio.monitoredFrameworks) {
                sendSfdrBulkDataRequest(enrichedPortfolio, monitoringPeriods)
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
         * Creates Bulk Data Request based on the company sector
         * @param enrichedPortfolio: enrichment of the given portfolio
         * @param monitoringPeriods: the monitoring periods
         * @param selector: the getter function for (non)-financial company Ids
         * @param dataTypes: the chosen frameworks
         */
        private fun sendBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
            selector: (EnrichedPortfolio) -> Set<String>,
            dataTypes: Set<BulkDataRequest.DataTypes>,
        ) {
            val companyIds = selector(enrichedPortfolio)
            if (companyIds.isEmpty()) {
                return
            }

            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(
                    companyIdentifiers = selector(enrichedPortfolio),
                    dataTypes = dataTypes,
                    reportingPeriods = monitoringPeriods,
                    notifyMeImmediately = false,
                ),
                enrichedPortfolio.userId,
            )
        }

        /**
         * Creates Bulk Data Request for "financials" companies
         */
        private fun sendFinancialBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = sendBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getFinancialsCompanyIds,
            setOf(
                BulkDataRequest.DataTypes.eutaxonomyMinusFinancials,
                BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
            ),
        )

        /**
         * Creates Bulk Data Request for non-"financials" companies
         */
        private fun sendNonFinancialBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = sendBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getNonFinancialsCompanyIds,
            setOf(
                BulkDataRequest.DataTypes.eutaxonomyMinusNonMinusFinancials,
                BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
            ),
        )

        /**
         * Creates Bulk Data Request for companies without sector
         */
        private fun sendFinancialAndNonFinancialBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = sendBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getUnsectorizedCompanyIds,
            setOf(
                BulkDataRequest.DataTypes.eutaxonomyMinusFinancials,
                BulkDataRequest.DataTypes.eutaxonomyMinusNonMinusFinancials,
                BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
            ),
        )

        private fun sendSfdrBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = sendBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getAllCompanyIds,
            setOf(
                BulkDataRequest.DataTypes.sfdr,
            ),
        )
    }
