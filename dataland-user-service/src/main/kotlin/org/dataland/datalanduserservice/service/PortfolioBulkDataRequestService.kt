package org.dataland.datalanduserservice.service

import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.BulkDataRequest
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service to create Bulk Data Requests upon Portfolio or Monitoring changes.
 */
@Service
class PortfolioBulkDataRequestService
    @Autowired
    constructor(
        private val requestControllerApi: RequestControllerApi,
        private val portfolioEnrichmentService: PortfolioEnrichmentService,
    ) {
        /**
         * Sends Bulk Data Requests with respect to the Monitoring Status and Company Sectors.
         */
        fun sendBulkDataRequestIfMonitored(portfolio: BasePortfolio) {
            if (portfolio.isMonitored) {
                val enrichedPortfolio = portfolioEnrichmentService.getEnrichedPortfolio(portfolio)
                val monitoringPeriods = getMonitoringPeriods(portfolio.startingMonitoringPeriod)

                if ("eutaxonomy" in portfolio.monitoredFrameworks) {
                    sendFinancialBulkDataRequest(enrichedPortfolio, monitoringPeriods)
                    sendNonFinancialBulkDataRequest(enrichedPortfolio, monitoringPeriods)
                    sendUndefinedBulkDataRequest(enrichedPortfolio, monitoringPeriods)
                }

                if ("sfdr" in portfolio.monitoredFrameworks) {
                    sendSfdrBulkDataRequest(enrichedPortfolio, monitoringPeriods)
                }
            }
        }

        private fun getCompanyIdsBySector(
            portfolio: EnrichedPortfolio,
            filterFunction: (String?) -> Boolean,
        ): Set<String> =
            portfolio.entries
                .filter { filterFunction(it.sector) }
                .map { it.companyId }
                .toSet()

        private fun getFinancialsCompanyIds(portfolio: EnrichedPortfolio) = getCompanyIdsBySector(portfolio) { it == "financials" }

        private fun getUndefinedCompanyIds(portfolio: EnrichedPortfolio) = getCompanyIdsBySector(portfolio) { it == null }

        private fun getNonFinancialsCompanyIds(portfolio: EnrichedPortfolio) =
            getCompanyIdsBySector(portfolio) { it != null && it != "financials" }

        private fun getAllCompanyIds(portfolio: EnrichedPortfolio): Set<String> = portfolio.entries.map { it.companyId }.toSet()

        private fun getMonitoringPeriods(startingMonitoringPeriod: String?): Set<String> {
            val startingMonitoringPeriodInt =
                startingMonitoringPeriod?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid start year: '$startingMonitoringPeriod' is not a valid number")
            return (startingMonitoringPeriodInt until 2025)
                .map { it.toString() }
                .toSet()
        }

        private fun sendBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
            selector: (EnrichedPortfolio) -> Set<String>,
            types: Set<BulkDataRequest.DataTypes>,
        ) {
            requestControllerApi.postBulkDataRequest(
                BulkDataRequest(
                    companyIdentifiers = selector(enrichedPortfolio),
                    dataTypes = types,
                    reportingPeriods = monitoringPeriods,
                    notifyMeImmediately = false,
                ),
            )
        }

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

        private fun sendUndefinedBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = sendBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getUndefinedCompanyIds,
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
