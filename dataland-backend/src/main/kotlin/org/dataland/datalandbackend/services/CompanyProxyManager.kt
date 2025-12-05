package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.model.proxies.CompanyProxyFilter
import org.dataland.datalandbackend.model.proxies.StoredCompanyProxy
import org.dataland.datalandbackend.repositories.CompanyProxyRepository
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
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
 * A rule describes which data of a proxied company can be substituted
 * by data of a proxy company, optionally restricted by frameworks and
 * reporting periods.
 */
@Service
class CompanyProxyManager
    @Autowired
    constructor(
        private val companyDataProxyRuleRepository: CompanyProxyRepository,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Returns the CompanyProxyEntity for the given proxyId.
         *
         * @throws InvalidInputApiException if no proxy rule exists for the given id.
         */
        @Transactional(readOnly = true)
        fun getCompanyProxyById(proxyId: UUID): StoredCompanyProxy = retrieveCompanyProxyEntityById(proxyId).toStoredCompanyProxy()

        private fun retrieveCompanyProxyEntityById(proxyId: UUID): CompanyProxyEntity =
            companyDataProxyRuleRepository
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
            proxiedCompanyId: UUID?,
            proxyCompanyId: UUID?,
            frameworks: Set<String>?,
            reportingPeriods: Set<String>?,
            chunkSize: Int = 100,
            chunkIndex: Int = 0,
        ): List<StoredCompanyProxy> {
            val frameworksSet = frameworks ?: emptySet()
            val reportingPeriodsSet = reportingPeriods ?: emptySet()

            val page =
                companyDataProxyRuleRepository.findByFilters(
                    CompanyProxyFilter(
                        proxiedCompanyId = proxiedCompanyId,
                        proxyCompanyId = proxyCompanyId,
                        frameworks = frameworksSet,
                        frameworksEmpty = frameworksSet.isEmpty(),
                        reportingPeriods = reportingPeriodsSet,
                        reportingPeriodsEmpty = reportingPeriodsSet.isEmpty(),
                    ),
                    pageable = PageRequest.of(chunkIndex, chunkSize),
                )

            return page.content.map { it.toStoredCompanyProxy() }
        }

        /**
         * Adds a new proxy rule based on the given domain model.
         *
         * Checks for existing conflicting rules to avoid duplicates.
         *
         * @throws InvalidInputApiException if a conflicting rule already exists.
         */
        @Transactional
        fun addProxyRelation(candidateProxy: CompanyProxy<UUID>): CompanyProxyEntity {
            val proxiedCompanyId = candidateProxy.proxiedCompanyId
            val proxyCompanyId = candidateProxy.proxyCompanyId

            logger.info(
                "Adding proxy relation for proxiedCompanyId=$proxiedCompanyId, " +
                    "proxyCompanyId=$proxyCompanyId, " +
                    "framework='${candidateProxy.framework}', " +
                    "reportingPeriod='${candidateProxy.reportingPeriod}'",
            )
            logger.info("Checking for existing proxy relations to avoid duplicates...")

            val existingProxies = companyDataProxyRuleRepository.findAllByProxiedCompanyId(candidateProxy.proxiedCompanyId)
            assertConflictingProxies(existingProxies, candidateProxy)

            val entity =
                CompanyProxyEntity(
                    proxiedCompanyId = proxiedCompanyId,
                    proxyCompanyId = proxyCompanyId,
                    framework = candidateProxy.framework,
                    reportingPeriod = candidateProxy.reportingPeriod,
                )

            val saved = companyDataProxyRuleRepository.save(entity)
            logger.info(
                "Saved proxy rule with id=${saved.proxyId} for proxiedCompanyId=$proxiedCompanyId, " +
                    "proxyCompanyId=$proxyCompanyId",
            )

            return saved
        }

        private fun assertConflictingProxies(
            existingProxies: List<CompanyProxyEntity>,
            candidateProxy: CompanyProxy<UUID>,
        ) {
            val conflictingProxies = findConflictingProxies(existingProxies, candidateProxy)

            if (conflictingProxies.isNotEmpty()) {
                throw InvalidInputApiException(
                    summary = "Conflicting proxy relation exists",
                    message =
                        "A conflicting proxy relation exists. " +
                            "Conflicting proxyIds: ${
                                conflictingProxies.joinToString(", ") { it.proxyId.toString() }
                            }.",
                )
            }
        }

        private fun findConflictingProxies(
            existingProxies: List<CompanyProxyEntity>,
            candidateProxy: CompanyProxy<UUID>,
        ): List<CompanyProxyEntity> =
            when {
                candidateProxy.framework.isNullOrEmpty() && candidateProxy.reportingPeriod.isNullOrEmpty() -> {
                    existingProxies
                }

                candidateProxy.framework.isNullOrEmpty() -> {
                    existingProxies.filter {
                        !it.framework.isNullOrEmpty() ||
                            it.reportingPeriod.isNullOrEmpty() ||
                            it.reportingPeriod == candidateProxy.reportingPeriod
                    }
                }

                candidateProxy.reportingPeriod.isNullOrEmpty() -> {
                    existingProxies.filter {
                        !it.reportingPeriod.isNullOrEmpty() ||
                            it.framework.isNullOrEmpty() ||
                            it.framework == candidateProxy.framework
                    }
                }

                else -> {
                    existingProxies.filter {
                        it.framework == candidateProxy.framework &&
                            it.reportingPeriod == candidateProxy.reportingPeriod ||
                            it.framework.isNullOrEmpty() ||
                            it.reportingPeriod.isNullOrEmpty()
                    }
                }
            }

        /**
         * Deletes all proxy rules for a given (proxiedCompanyId, proxyCompanyId) pair.
         *
         * If no rules exist for this pair, a ResourceNotFoundApiException is thrown.
         */
        @Transactional
        fun deleteProxyRelation(proxyId: UUID): CompanyProxyEntity {
            val existing =
                companyDataProxyRuleRepository
                    .findById(proxyId)
                    .orElseThrow {
                        ResourceNotFoundApiException(
                            "No proxy rule found for id=$proxyId",
                            message = "No proxy rule exists for the specified proxyId.",
                        )
                    }

            logger.info(
                "Deleting proxy rule with id=$proxyId " +
                    "(proxiedCompanyId=${existing.proxiedCompanyId}, " +
                    "proxyCompanyId=${existing.proxyCompanyId}, " +
                    "framework=${existing.framework}, reportingPeriod=${existing.reportingPeriod})",
            )

            companyDataProxyRuleRepository.delete(existing)

            return existing
        }

        /**
         * Edits an existing company proxy rule identified by proxyId
         * with the data from updatedCompanyProxy.
         *
         * Checks for existing conflicting rules to avoid duplicates.
         *
         * @throws InvalidInputApiException if a conflicting rule already exists.
         */
        @Transactional
        fun editCompanyProxy(
            proxyId: UUID,
            updatedCompanyProxy: CompanyProxy<UUID>,
        ): StoredCompanyProxy {
            val existingProxiesForCompany =
                companyDataProxyRuleRepository
                    .findAllByProxiedCompanyId(updatedCompanyProxy.proxiedCompanyId)
                    .filter { it.proxyId != proxyId }
            assertConflictingProxies(existingProxiesForCompany, updatedCompanyProxy)

            val existing = retrieveCompanyProxyEntityById(proxyId)
            existing.proxiedCompanyId = updatedCompanyProxy.proxiedCompanyId
            existing.proxyCompanyId = updatedCompanyProxy.proxyCompanyId
            existing.framework = updatedCompanyProxy.framework
            existing.reportingPeriod = updatedCompanyProxy.reportingPeriod

            return companyDataProxyRuleRepository.save(existing).toStoredCompanyProxy()
        }
    }
