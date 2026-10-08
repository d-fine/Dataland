package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.model.proxies.CompanyProxyFilter
import org.dataland.datalandbackend.model.proxies.StoredCompanyProxy
import org.dataland.datalandbackend.repositories.CompanyProxyRepository
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Manager for creating and querying company data proxy rules.
 *
 * A rule describes which data of a child company can be substituted
 * by data of a parent company during a range of reporting periods.
 */
@Service
class CompanyProxyManager
    @Autowired
    constructor(
        private val companyProxyRepository: CompanyProxyRepository,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Returns the CompanyProxyEntity for the given proxyId.
         *
         * @throws ResourceNotFoundApiException if no proxy rule exists for the given id.
         */
        @Transactional(readOnly = true)
        fun getCompanyProxyById(proxyId: UUID): StoredCompanyProxy = retrieveCompanyProxyEntityById(proxyId).toStoredCompanyProxy()

        private fun retrieveCompanyProxyEntityById(proxyId: UUID): CompanyProxyEntity =
            companyProxyRepository
                .findById(proxyId)
                .orElseThrow {
                    ResourceNotFoundApiException(
                        summary = "Company proxy not found.",
                        message = "No company proxy rule exists for the proxyId=$proxyId.",
                    )
                }

        /**
         * Returns all stored company proxies matching the given filters as domain models.
         *
         * If a filter parameter is null or empty, it is ignored.
         *
         * Results are paginated using chunkSize and chunkIndex.
         */
        @Transactional(readOnly = true)
        fun getCompanyProxiesByFilters(
            childCompanyId: UUID?,
            parentCompanyId: UUID?,
            chunkSize: Int = 100,
            chunkIndex: Int = 0,
        ): List<StoredCompanyProxy> {
            val page =
                companyProxyRepository.findByFilters(
                    CompanyProxyFilter(
                        childCompanyId = childCompanyId,
                        parentCompanyId = parentCompanyId,
                    ),
                    pageable = PageRequest.of(chunkIndex, chunkSize),
                )

            return page.content.map { it.toStoredCompanyProxy() }
        }

        /**
         * Adds a new proxy rule based on the given domain model.
         */
        @Transactional
        fun addProxyRelation(
            candidateProxy: CompanyProxy<UUID>,
            uploaderUserId: String,
        ): CompanyProxyEntity {
            val childCompanyId = candidateProxy.childCompanyId
            val parentCompanyId = candidateProxy.parentCompanyId

            logger.info(
                "Adding proxy relation for childCompanyId=$childCompanyId, " +
                    "parentCompanyId=$parentCompanyId, " +
                    "validFromReportingPeriod='${candidateProxy.validFromReportingPeriod}', " +
                    "validToReportingPeriod='${candidateProxy.validToReportingPeriod}'",
            )

            val entity =
                CompanyProxyEntity(
                    childCompanyId = childCompanyId,
                    parentCompanyId = parentCompanyId,
                    validFromReportingPeriod = candidateProxy.validFromReportingPeriod,
                    validToReportingPeriod = candidateProxy.validToReportingPeriod,
                    uploadTimestamp = System.currentTimeMillis(),
                    uploaderUserId = uploaderUserId,
                )

            val saved = companyProxyRepository.save(entity)
            logger.info(
                "Saved proxy rule with id=${saved.proxyId} for childCompanyId=$childCompanyId, " +
                    "parentCompanyId=$parentCompanyId",
            )

            return saved
        }

        /**
         * Deletes all proxy rules for a given (childCompanyId, parentCompanyId) pair.
         *
         * If no rules exist for this pair, a ResourceNotFoundApiException is thrown.
         */
        @Transactional
        fun deleteProxyRelation(proxyId: UUID): CompanyProxyEntity {
            val existing =
                companyProxyRepository
                    .findById(proxyId)
                    .orElseThrow {
                        ResourceNotFoundApiException(
                            "No proxy rule found for id=$proxyId",
                            message = "No proxy rule exists for the specified proxyId.",
                        )
                    }

            logger.info(
                "Deleting proxy rule with id=$proxyId " +
                    "(childCompanyId=${existing.childCompanyId}, " +
                    "parentCompanyId=${existing.parentCompanyId}, " +
                    "validFromReportingPeriod=${existing.validFromReportingPeriod}, " +
                    "validToReportingPeriod=${existing.validToReportingPeriod})",
            )

            companyProxyRepository.delete(existing)

            return existing
        }

        /**
         * Edits an existing company proxy rule identified by proxyId
         * with the data from updatedCompanyProxy.
         */
        @Transactional
        fun editCompanyProxy(
            proxyId: UUID,
            updatedCompanyProxy: CompanyProxy<UUID>,
            uploaderUserId: String,
        ): StoredCompanyProxy {
            val existing = retrieveCompanyProxyEntityById(proxyId)
            existing.childCompanyId = updatedCompanyProxy.childCompanyId
            existing.parentCompanyId = updatedCompanyProxy.parentCompanyId
            existing.validFromReportingPeriod = updatedCompanyProxy.validFromReportingPeriod
            existing.validToReportingPeriod = updatedCompanyProxy.validToReportingPeriod
            existing.uploadTimestamp = System.currentTimeMillis()
            existing.uploaderUserId = uploaderUserId

            return companyProxyRepository.save(existing).toStoredCompanyProxy()
        }
    }
