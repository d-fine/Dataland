package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyProxyApi
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.model.proxies.CompanyProxyRelationResponse
import org.dataland.datalandbackend.model.proxies.CompanyProxyRequest
import org.dataland.datalandbackend.services.CompanyProxyManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the company data proxy rule endpoints.
 *
 * @param companyProxyManager the service to handle creation and retrieval of proxy rules
 */
@RestController
class CompanyProxyController
    @Autowired
    constructor(
        private val companyProxyManager: CompanyProxyManager,
    ) : CompanyProxyApi {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * POST /company-data-proxy-relation
         */
        override fun postCompanyProxy(companyProxy: CompanyProxyRequest): ResponseEntity<List<CompanyProxyRelationResponse>> {
            logger.info(
                "Received request to upsert proxy relation for " +
                    "proxiedCompanyId='${companyProxy.proxiedCompanyId}', " +
                    "proxyCompanyId='${companyProxy.proxyCompanyId}', " +
                    "frameworks='${companyProxy.frameworks}', " +
                    "reportingPeriods='${companyProxy.reportingPeriods}'",
            )

            val domainRelation =
                CompanyProxy(
                    proxiedCompanyId = UUID.fromString(companyProxy.proxiedCompanyId),
                    proxyCompanyId = UUID.fromString(companyProxy.proxyCompanyId),
                    frameworks = companyProxy.frameworks,
                    reportingPeriods = companyProxy.reportingPeriods,
                )

            val savedEntities = companyProxyManager.addProxyRelation(domainRelation)

            val responseBody =
                savedEntities.map {
                    CompanyProxyRelationResponse(
                        proxyId = it.proxyId.toString(),
                        proxiedCompanyId = it.proxiedCompanyId.toString(),
                        proxyCompanyId = it.proxyCompanyId.toString(),
                        framework = it.framework,
                        reportingPeriod = it.reportingPeriod,
                    )
                }

            return ResponseEntity.ok(responseBody)
        }

        /**
         * GET /company-data-proxy-relation
         */
        override fun getCompanyProxy(proxiedCompanyId: String): ResponseEntity<List<CompanyProxy>> {
            logger.info(
                "Received request to get proxy relation for " +
                    "proxiedCompanyId='$proxiedCompanyId'",
            )
            val relation =
                companyProxyManager.getProxyRelation(UUID.fromString(proxiedCompanyId))
            return ResponseEntity.ok(relation)
        }

        /**
         * DELETE /company-proxies/company-proxy
         */
        override fun deleteCompanyProxy(technicalId: String): ResponseEntity<CompanyProxyRelationResponse> {
            logger.info("Received request to delete proxy rule with technicalId='$technicalId'")

            val uuid = UUID.fromString(technicalId)

            val deleted = companyProxyManager.deleteProxyRelation(uuid)

            val response =
                CompanyProxyRelationResponse(
                    proxyId = deleted.proxyId.toString(),
                    proxiedCompanyId = deleted.proxiedCompanyId.toString(),
                    proxyCompanyId = deleted.proxyCompanyId.toString(),
                    framework = deleted.framework,
                    reportingPeriod = deleted.reportingPeriod,
                )

            return ResponseEntity.ok(response)
        }
    }
