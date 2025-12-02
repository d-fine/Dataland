package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.dataland.datalandbackend.model.proxies.CompanyProxy
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
                    proxiedCompanyId = proxiedCompanyId,
                    proxyCompanyId = proxyCompanyId,
                    frameworks = frameworksSet,
                    frameworksEmpty = frameworksSet.isEmpty(),
                    reportingPeriods = reportingPeriodsSet,
                    reportingPeriodsEmpty = reportingPeriodsSet.isEmpty(),
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
        fun addProxyRelation(relation: CompanyProxy<UUID>): CompanyProxyEntity {
            val proxiedCompanyId = relation.proxiedCompanyId
            val proxyCompanyId = relation.proxyCompanyId

            logger.info(
                "Adding proxy relation for proxiedCompanyId=$proxiedCompanyId, " +
                    "proxyCompanyId=$proxyCompanyId, " +
                    "framework='${relation.framework}', " +
                    "reportingPeriod='${relation.reportingPeriod}'",
            )
            logger.info("Checking for existing proxy relations to avoid duplicates...")

            val existingProxies = companyDataProxyRuleRepository.findAllByProxiedCompanyId(proxiedCompanyId)
            val conflictingProxies = findConflictingProxies(existingProxies, relation)

            if (conflictingProxies.isNotEmpty()) {
                throw InvalidInputApiException(
                    summary = "Conflicting proxy relation already exists",
                    message =
                        "A conflicting proxy relation already exists. " +
                            "Conflicting proxyIds: ${
                                conflictingProxies.joinToString(", ") { it.proxyId.toString() }
                            }.",
                )
            }

            val entity =
                CompanyProxyEntity(
                    proxiedCompanyId = proxiedCompanyId,
                    proxyCompanyId = proxyCompanyId,
                    framework = relation.framework,
                    reportingPeriod = relation.reportingPeriod,
                )

            val saved = companyDataProxyRuleRepository.save(entity)
            logger.info(
                "Saved proxy rule with id=${saved.proxyId} for proxiedCompanyId=$proxiedCompanyId, " +
                    "proxyCompanyId=$proxyCompanyId",
            )

            return saved
        }

        private fun findConflictingProxies(
            existingProxies: List<CompanyProxyEntity>,
            relation: CompanyProxy<UUID>,
        ): List<CompanyProxyEntity> =
            when {
                relation.framework.isNullOrEmpty() && relation.reportingPeriod.isNullOrEmpty() ->
                    existingProxies

                relation.framework.isNullOrEmpty() ->
                    existingProxies.filter {
                        !it.framework.isNullOrEmpty() ||
                            it.reportingPeriod.isNullOrEmpty() ||
                            it.reportingPeriod == relation.reportingPeriod
                    }

                relation.reportingPeriod.isNullOrEmpty() ->
                    existingProxies.filter {
                        !it.reportingPeriod.isNullOrEmpty() ||
                            it.framework.isNullOrEmpty() ||
                            it.framework == relation.framework
                    }

                else ->
                    existingProxies.filter {
                        it.framework == relation.framework &&
                            it.reportingPeriod == relation.reportingPeriod ||
                            it.framework.isNullOrEmpty() ||
                            it.reportingPeriod.isNullOrEmpty()
                    }
            }

        /**
         * Returns all proxy rules for a given proxiedCompanyId as domain models.
         *
         * Semantics:
         *  - Each row in company_proxy_relations becomes one CompanyProxy.
         *  - framework = null → applies to all frameworks
         *  - reportingPeriod = null → applies to all reporting periods
         */
        @Transactional(readOnly = true)
        fun getProxyRelations(proxiedCompanyId: UUID): List<CompanyProxy<UUID>> {
            val entities =
                companyDataProxyRuleRepository
                    .findAllByProxiedCompanyId(proxiedCompanyId)

            if (entities.isEmpty()) {
                throw InvalidInputApiException(
                    "No proxy rules found for proxiedCompanyId=$proxiedCompanyId",
                    message = "No proxy rules exist for the specified company.",
                )
            }

            return entities.map { row ->
                CompanyProxy(
                    proxiedCompanyId = row.proxiedCompanyId,
                    proxyCompanyId = row.proxyCompanyId,
                    framework = row.framework, // null => all frameworks
                    reportingPeriod = row.reportingPeriod, // null => all periods
                )
            }
        }

        /**
         * Deletes all proxy rules for a given (proxiedCompanyId, proxyCompanyId) pair.
         *
         * If no rules exist for this pair, an InvalidInputApiException is thrown.
         */
        @Transactional
        fun deleteProxyRelation(proxyId: UUID): CompanyProxyEntity {
            val existing =
                companyDataProxyRuleRepository
                    .findById(proxyId)
                    .orElseThrow {
                        InvalidInputApiException(
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

        @Transactional
        fun editCompanyProxy(
            proxyId: UUID,
            updatedCompanyProxy: CompanyProxy<UUID>,
        ): StoredCompanyProxy {
            val existing = retrieveCompanyProxyEntityById(proxyId)

            existing.proxiedCompanyId = updatedCompanyProxy.proxiedCompanyId
            existing.proxyCompanyId = updatedCompanyProxy.proxyCompanyId
            existing.framework = updatedCompanyProxy.framework
            existing.reportingPeriod = updatedCompanyProxy.reportingPeriod

            return companyDataProxyRuleRepository.save(existing).toStoredCompanyProxy()
        }
    }
