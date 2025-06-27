package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID.randomUUID

/**
 * Service to publish Bulk Data Requests Messages upon Portfolio or Monitoring status changes.
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
         * Publishes Bulk Data Request Messages for monitored portfolios.
         */
        fun publishBulkDataRequestMessageIfMonitored(portfolio: BasePortfolio) {
            if (!portfolio.isMonitored) {
                return
            }
            val enrichedPortfolio = portfolioEnrichmentService.getEnrichedPortfolio(portfolio)
            val monitoringPeriods = getMonitoringPeriods(portfolio.startingMonitoringPeriod)

            if ("eutaxonomy" in portfolio.monitoredFrameworks) {
                publishFinancialBulkDataRequestMessage(enrichedPortfolio, monitoringPeriods)
                publishNonFinancialBulkDataRequestMessage(enrichedPortfolio, monitoringPeriods)
                publishFinancialAndNonFinancialBulkDataRequestMessage(enrichedPortfolio, monitoringPeriods)
            }

            if ("sfdr" in portfolio.monitoredFrameworks) {
                publishSfdrBulkDataRequestMessage(enrichedPortfolio, monitoringPeriods)
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
         * Publishes Bulk Data Request Messages based on the company sector
         * @param enrichedPortfolio: enrichment of the given portfolio
         * @param reportingPeriods: the monitoring periods
         * @param selector: the getter function for (non)-financial company Ids
         * @param monitoredFrameworks: the chosen frameworks
         */
        private fun publishBulkDataRequestMessage(
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
         * Publishes EU Taxonomy Bulk Data Request Messages for "financials" companies
         */
        private fun publishFinancialBulkDataRequestMessage(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = publishBulkDataRequestMessage(
            enrichedPortfolio, monitoringPeriods, ::getFinancialsCompanyIds,
            setOf(DataTypeEnum.eutaxonomyMinusFinancials.value, DataTypeEnum.nuclearMinusAndMinusGas.value),
        )

        /**
         * Publishes EU Taxonomy Bulk Data Request Messages for non-"financials" companies
         */
        private fun publishNonFinancialBulkDataRequestMessage(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = publishBulkDataRequestMessage(
            enrichedPortfolio, monitoringPeriods, ::getNonFinancialsCompanyIds,
            setOf(
                DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value,
                DataTypeEnum.nuclearMinusAndMinusGas.value,
            ),
        )

        /**
         * Publishes EU Taxonomy Bulk Data Request Messages for companies without sector
         */
        private fun publishFinancialAndNonFinancialBulkDataRequestMessage(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = publishBulkDataRequestMessage(
            enrichedPortfolio, monitoringPeriods, ::getUnsectorizedCompanyIds,
            setOf(
                DataTypeEnum.eutaxonomyMinusFinancials.value,
                DataTypeEnum.nuclearMinusAndMinusGas.value,
                DataTypeEnum.eutaxonomyMinusNonMinusFinancials.value,
            ),
        )

        /**
         * Publishes SFDR Bulk Data Request Messages for all companies
         */

        private fun publishSfdrBulkDataRequestMessage(
            enrichedPortfolio: EnrichedPortfolio,
            monitoringPeriods: Set<String>,
        ) = publishBulkDataRequestMessage(
            enrichedPortfolio, monitoringPeriods, ::getAllCompanyIds,
            setOf(
                DataTypeEnum.sfdr.value,
            ),
        )
    }
