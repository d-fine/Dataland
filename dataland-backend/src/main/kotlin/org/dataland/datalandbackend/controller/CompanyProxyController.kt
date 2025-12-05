package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.CompanyProxyApi
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.model.proxies.StoredCompanyProxy
import org.dataland.datalandbackend.services.CompanyProxyManager
import org.dataland.datalandbackendutils.utils.ValidationUtils
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
        override fun postCompanyProxy(companyProxy: CompanyProxy<String>): ResponseEntity<StoredCompanyProxy> {
            logger.info(
                "Received request to create proxy relation for " +
                    "proxiedCompanyId='${companyProxy.proxiedCompanyId}', " +
                    "proxyCompanyId='${companyProxy.proxyCompanyId}', " +
                    "framework='${companyProxy.framework}', " +
                    "reportingPeriod='${companyProxy.reportingPeriod}'",
            )

            val savedEntity = companyProxyManager.addProxyRelation(companyProxy.convertToCompanyProxyWithUUIDs())

            return ResponseEntity.ok(savedEntity.toStoredCompanyProxy())
        }

        /**
         * GET /company-data-proxy-relation
         */
        override fun getCompanyProxyById(proxyId: String): ResponseEntity<StoredCompanyProxy> {
            logger.info(
                "Received request to get proxy relation for " +
                    "proxyId='$proxyId'",
            )
            return ResponseEntity.ok(
                companyProxyManager.getCompanyProxyById(UUID.fromString(proxyId)),
            )
        }

        /**
         * GET /company-proxies/company-proxy
         */
        override fun searchCompanyProxies(
            proxiedCompanyId: String?,
            proxyCompanyId: String?,
            frameworks: Set<String>?,
            reportingPeriods: Set<String>?,
            chunkSize: Int,
            chunkIndex: Int,
        ): ResponseEntity<List<StoredCompanyProxy>> {
            logger.info("Received request to get all company proxies")
            return ResponseEntity.ok(
                companyProxyManager.getCompanyProxiesByFilters(
                    proxiedCompanyId = proxiedCompanyId?.let { ValidationUtils.convertToUUID(it) },
                    proxyCompanyId = proxyCompanyId?.let { ValidationUtils.convertToUUID(it) },
                    frameworks = frameworks,
                    reportingPeriods = reportingPeriods,
                    chunkSize = chunkSize,
                    chunkIndex = chunkIndex,
                ),
            )
        }

        /**
         * DELETE /company-proxies/company-proxy
         */
        override fun deleteCompanyProxy(proxyId: String): ResponseEntity<StoredCompanyProxy> {
            logger.info("Received request to delete proxy rule with proxyId='$proxyId'")

            val uuid = ValidationUtils.convertToUUID(proxyId)

            val deleted = companyProxyManager.deleteProxyRelation(uuid)

            return ResponseEntity.ok(deleted.toStoredCompanyProxy())
        }

        /**
         * PUT /company-proxies/company-proxy
         */
        override fun putCompanyProxy(
            proxyId: String,
            companyProxy: CompanyProxy<String>,
        ): ResponseEntity<StoredCompanyProxy> {
            logger.info(
                "Received request to update proxy rule for " +
                    "proxiedCompanyId = '${companyProxy.proxiedCompanyId}', proxyCompanyId = '${companyProxy.proxyCompanyId}'",
            )
            return ResponseEntity.ok(
                companyProxyManager.editCompanyProxy(
                    proxyId = ValidationUtils.convertToUUID(proxyId),
                    updatedCompanyProxy = companyProxy.convertToCompanyProxyWithUUIDs(),
                ),
            )
        }
    }
