package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyProxyApi
import org.dataland.datalandbackend.model.proxies.BulkCompanyProxy
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
         * POST /company-data-proxy-rules
         */
        override fun postCompanyProxy(bulkCompanyProxy: BulkCompanyProxy): ResponseEntity<Void> {
            logger.info(
                "Received request to upsert proxy rules for " +
                    "proxiedCompanyId='${bulkCompanyProxy.proxiedCompanyId}', " +
                    "proxyCompanyId='${bulkCompanyProxy.proxyCompanyId}', " +
                    "frameworks='${bulkCompanyProxy.frameworks}', " +
                    "reportingPeriods='${bulkCompanyProxy.reportingPeriods}'",
            )

            companyProxyManager.addProxyRelation(bulkCompanyProxy)
            return ResponseEntity.ok().build()
        }

        /**
         * GET /company-data-proxy-rules
         */
        override fun getCompanyProxy(proxiedCompanyId: String): ResponseEntity<List<BulkCompanyProxy>> {
            logger.info(
                "Received request to get proxy rules for " +
                    "proxiedCompanyId='$proxiedCompanyId'",
            )

            val rules =
                companyProxyManager.getProxyRelation(UUID.fromString(proxiedCompanyId))
            return ResponseEntity.ok(rules)
        }

        /**
         * DELETE /company-proxies/company-proxy
         */
        override fun deleteCompanyProxy(
            proxiedCompanyId: String,
            proxyCompanyId: String,
        ): ResponseEntity<Void> {
            logger.info(
                "Received request to delete proxy rules for " +
                    "proxiedCompanyId='$proxiedCompanyId', proxyCompanyId='$proxyCompanyId'",
            )

            // TODO: Do we want to delete all relations for a given company pair?
            companyProxyManager.deleteProxyRelation(UUID.fromString(proxiedCompanyId), UUID.fromString(proxyCompanyId))
            return ResponseEntity.noContent().build()
        }
    }
