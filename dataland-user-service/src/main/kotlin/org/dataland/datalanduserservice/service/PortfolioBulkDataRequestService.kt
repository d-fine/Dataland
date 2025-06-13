package org.dataland.datalanduserservice.service
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.BulkDataRequest
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Year

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
        fun sendBulkDataRequest(portfolio: BasePortfolio) {
            if (portfolio.isMonitored) {
                val enrichedPortfolio = portfolioEnrichmentService.getEnrichedPortfolio(portfolio)
                val monitoringPeriods = getMonitoringPeriods(portfolio.startingMonitoringPeriod)

                if ("eutaxonomy" in portfolio.monitoredFrameworks) {
                    requestControllerApi.postBulkDataRequest(
                        BulkDataRequest(
                            companyIdentifiers = getFinancialsCompanyIds(enrichedPortfolio),
                            dataTypes =
                                setOf(
                                    BulkDataRequest.DataTypes.eutaxonomyMinusFinancials,
                                    BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
                                ),
                            reportingPeriods = monitoringPeriods,
                            notifyMeImmediately = false,
                        ),
                    )

                    requestControllerApi.postBulkDataRequest(
                        BulkDataRequest(
                            companyIdentifiers = getNonFinancialsCompanyIds(enrichedPortfolio),
                            dataTypes =
                                setOf(
                                    BulkDataRequest.DataTypes.eutaxonomyMinusNonMinusFinancials,
                                    BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
                                ),
                            reportingPeriods = monitoringPeriods,
                            notifyMeImmediately = false,
                        ),
                    )

                    requestControllerApi.postBulkDataRequest(
                        BulkDataRequest(
                            companyIdentifiers = getUndefinedCompanyIds(enrichedPortfolio),
                            dataTypes =
                                setOf(
                                    BulkDataRequest.DataTypes.eutaxonomyMinusFinancials,
                                    BulkDataRequest.DataTypes.eutaxonomyMinusNonMinusFinancials,
                                    BulkDataRequest.DataTypes.nuclearMinusAndMinusGas,
                                ),
                            reportingPeriods = monitoringPeriods,
                            notifyMeImmediately = false,
                        ),
                    )
                }

                if ("sfdr" in portfolio.monitoredFrameworks) {
                    requestControllerApi.postBulkDataRequest(
                        BulkDataRequest(
                            companyIdentifiers = getAllCompanyIds(enrichedPortfolio),
                            dataTypes =
                                setOf(BulkDataRequest.DataTypes.sfdr),
                            reportingPeriods = monitoringPeriods,
                            notifyMeImmediately = false,
                        ),
                    )
                }
            }
        }

        private fun getFinancialsCompanyIds(portfolio: EnrichedPortfolio): Set<String> =
            portfolio.entries
                .filter { it.sector == "financials" }
                .map { it.companyId }
                .toSet()

        private fun getUndefinedCompanyIds(portfolio: EnrichedPortfolio): Set<String> =
            portfolio.entries
                .filter { it.sector == null }
                .map { it.companyId }
                .toSet()

        private fun getNonFinancialsCompanyIds(portfolio: EnrichedPortfolio): Set<String> =
            portfolio.entries
                .filter { it.sector !== null && it.sector !== "financials" }
                .map { it.companyId }
                .toSet()

        private fun getAllCompanyIds(portfolio: EnrichedPortfolio): Set<String> = portfolio.entries.map { it.companyId }.toSet()

        private fun getMonitoringPeriods(startingMonitoringPeriod: String?): Set<String> {
            val startingMonitoringPeriodInt =
                startingMonitoringPeriod?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid start year: '$startingMonitoringPeriod' is not a valid number")
            val currentYear = Year.now().value
            return (startingMonitoringPeriodInt until currentYear)
                .map { it.toString() }
                .toSet()
        }
    }
