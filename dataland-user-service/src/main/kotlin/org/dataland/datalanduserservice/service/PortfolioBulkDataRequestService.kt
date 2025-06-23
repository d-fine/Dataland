package org.dataland.datalanduserservice.service

import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID.randomUUID

/**
 * Service to create Bulk Data Requests upon Portfolio or Monitoring changes.
 * Currently restricted to Requests up to 2024. If required, change UPPER_BOUND accordingly.
 */
@Service
class PortfolioBulkDataRequestService
    @Autowired
    constructor(
        private val publisher: MessageQueuePublisher,
        private val portfolioEnrichmentService: PortfolioEnrichmentService,
    ) {
        companion object {
            const val UPPER_BOUND = 2025
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
         * @param reportingPeriods: the monitoring periods
         * @param selector: the getter function for (non)-financial company Ids
         * @param monitoredFrameworks: the chosen frameworks
         */
        private fun sendBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            reportingPeriods: Set<String>,
            selector: (EnrichedPortfolio) -> Set<String>,
            monitoredFrameworks: Set<String>,
        ) {
            val portfolioId = enrichedPortfolio.portfolioId
            val companyIds = selector(enrichedPortfolio)
            val correlationId = randomUUID().toString()
            if (companyIds.isEmpty()) {
                return
            }

            publisher.publishPortfolioUpdate(
                portfolioId,
                companyIds,
                monitoredFrameworks,
                reportingPeriods,
                correlationId,
            )
        }

        /**
         * Creates EU Taxonomy Bulk Data Request for "financials" companies
         */
        private fun sendFinancialBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = sendBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getFinancialsCompanyIds,
            setOf(
                "eutaxonomy-financials",
                "eutaxonomy-nuclear-and-gas",
            ),
        )

        /**
         * Creates EU Taxonomy Bulk Data Request for non-"financials" companies
         */
        private fun sendNonFinancialBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = sendBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getNonFinancialsCompanyIds,
            setOf(
                "eutaxonomy-non-minus-financials",
                "nuclear-and-gas",
            ),
        )

        /**
         * Creates EU Taxonomy Bulk Data Request for companies without sector
         */
        private fun sendFinancialAndNonFinancialBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = sendBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getUnsectorizedCompanyIds,
            setOf(
                "eutaxonomy-financials",
                "eutaxonomy-non-financials",
                "nuclear-and-gas,",
            ),
        )

        /**
         * Creates SFDR Bulk Data Request for all companies
         */

        private fun sendSfdrBulkDataRequest(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = sendBulkDataRequest(
            enrichedPortfolio, monitoringPeriods, ::getAllCompanyIds,
            setOf(
                "sfdr",
            ),
        )
    }
