package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.model.proxies.CompanyProxyFilter
import org.dataland.datalandbackend.model.proxies.StoredCompanyProxy
import org.dataland.datalandbackend.repositories.CompanyProxyRepository
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils
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
            frameworks: Set<String>?,
            reportingPeriods: Set<String>?,
            chunkSize: Int = 100,
            chunkIndex: Int = 0,
        ): List<StoredCompanyProxy> {
            val frameworksSet = frameworks ?: emptySet()
            val reportingPeriodsSet = reportingPeriods ?: emptySet()

            val page =
                companyProxyRepository.findByFilters(
                    CompanyProxyFilter(
                        childCompanyId = childCompanyId,
                        parentCompanyId = parentCompanyId,
                        frameworks = frameworksSet,
                        reportingPeriods = reportingPeriodsSet,
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
        fun addProxyRelation(
            candidateProxy: CompanyProxy<UUID>,
            uploaderUserId: String,
        ): CompanyProxyEntity {
            val childCompanyId = candidateProxy.childCompanyId
            val parentCompanyId = candidateProxy.parentCompanyId

            logger.info(
                "Adding proxy relation for childCompanyId=$childCompanyId, " +
                    "parentCompanyId=$parentCompanyId, " +
                    "framework='${candidateProxy.framework}', " +
                    "reportingPeriod='${candidateProxy.reportingPeriod}'",
            )
            logger.info("Checking for existing proxy relations to avoid duplicates...")

            val existingProxies = companyProxyRepository.findAllByChildCompanyId(candidateProxy.childCompanyId)
            assertNoConflictingProxiesExist(existingProxies, candidateProxy)

            val entity =
                CompanyProxyEntity(
                    childCompanyId = childCompanyId,
                    parentCompanyId = parentCompanyId,
                    framework = candidateProxy.framework,
                    reportingPeriod = candidateProxy.reportingPeriod,
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

        private fun assertNoConflictingProxiesExist(
            existingProxies: List<CompanyProxyEntity>,
            candidateProxy: CompanyProxy<UUID>,
        ) {
            val existingProxyEntriesThatConflict = findConflictingProxies(existingProxies, candidateProxy)

            if (existingProxyEntriesThatConflict.isNotEmpty()) {
                throw InvalidInputApiException(
                    summary = "Conflicting proxy relation exists",
                    message =
                        "A conflicting proxy relation exists. " +
                            "Conflicting proxyIds: ${
                                existingProxyEntriesThatConflict.toSet().joinToString(", ") { it.proxyId.toString() }
                            }.",
                )
            }
        }

        private fun findConflictingProxies(
            existingProxies: List<CompanyProxyEntity>,
            candidateProxy: CompanyProxy<UUID>,
        ): List<CompanyProxyEntity> {
            val candidateCombinations = getAllFrameworkAndReportingPeriodCombinationsForAProxyEntry(candidateProxy)
            val conflictingExistingProxies = mutableListOf<CompanyProxyEntity>()

            existingProxies.forEach { existingProxy ->
                val expandedExistingCombinations =
                    getAllFrameworkAndReportingPeriodCombinationsForAProxyEntry(
                        CompanyProxy(
                            childCompanyId = existingProxy.childCompanyId,
                            parentCompanyId = existingProxy.parentCompanyId,
                            framework = existingProxy.framework,
                            reportingPeriod = existingProxy.reportingPeriod,
                        ),
                    )
                if (candidateCombinations.intersect(expandedExistingCombinations).isNotEmpty()) {
                    conflictingExistingProxies.add(existingProxy)
                }
            }

            return conflictingExistingProxies
        }

        /**
         * Get all possible combinations of framework and reporting period for a proxy entry.
         * If either is null ("all"), all valid combinations are returned accordingly.
         */
        private fun getAllFrameworkAndReportingPeriodCombinationsForAProxyEntry(
            companyProxy: CompanyProxy<UUID>,
        ): Set<Pair<String, String>> {
            val allReportingPeriods = (ValidationUtils.REPORTING_PERIOD_MINIMUM..ValidationUtils.REPORTING_PERIOD_MAXIMUM).toList()
            val reportingPeriods = if (companyProxy.reportingPeriod == null) allReportingPeriods else listOf(companyProxy.reportingPeriod)
            val frameworks = if (companyProxy.framework == null) DataType.values else listOf(companyProxy.framework)

            return reportingPeriods
                .flatMap { reportingPeriod ->
                    frameworks.map { framework ->
                        Pair(framework.toString(), reportingPeriod.toString())
                    }
                }.toSet()
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
                    "framework=${existing.framework}, reportingPeriod=${existing.reportingPeriod})",
            )

            companyProxyRepository.delete(existing)

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
            uploaderUserId: String,
        ): StoredCompanyProxy {
            val existingProxiesForCompany =
                companyProxyRepository
                    .findAllByChildCompanyId(updatedCompanyProxy.childCompanyId)
                    .filter { it.proxyId != proxyId }
            assertNoConflictingProxiesExist(existingProxiesForCompany, updatedCompanyProxy)

            val existing = retrieveCompanyProxyEntityById(proxyId)
            existing.childCompanyId = updatedCompanyProxy.childCompanyId
            existing.parentCompanyId = updatedCompanyProxy.parentCompanyId
            existing.framework = updatedCompanyProxy.framework
            existing.reportingPeriod = updatedCompanyProxy.reportingPeriod
            existing.uploadTimestamp = System.currentTimeMillis()
            existing.uploaderUserId = uploaderUserId

            return companyProxyRepository.save(existing).toStoredCompanyProxy()
        }
    }
