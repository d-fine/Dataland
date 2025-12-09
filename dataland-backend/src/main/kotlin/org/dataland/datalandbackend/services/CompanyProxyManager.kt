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
                companyProxyRepository.findByFilters(
                    CompanyProxyFilter(
                        proxiedCompanyId = proxiedCompanyId,
                        proxyCompanyId = proxyCompanyId,
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

            val existingProxies = companyProxyRepository.findAllByProxiedCompanyId(candidateProxy.proxiedCompanyId)
            assertConflictingProxies(existingProxies, candidateProxy)

            val entity =
                CompanyProxyEntity(
                    proxiedCompanyId = proxiedCompanyId,
                    proxyCompanyId = proxyCompanyId,
                    framework = candidateProxy.framework,
                    reportingPeriod = candidateProxy.reportingPeriod,
                )

            val saved = companyProxyRepository.save(entity)
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
            val conflicts = findConflictingProxies(existingProxies, candidateProxy)
            val (existingProxyEntriesThatConflict, _) = conflicts.unzip()

            if (existingProxyEntriesThatConflict.isNotEmpty()) {
                throw InvalidInputApiException(
                    summary = "Conflicting proxy relation exists",
                    message =
                        "A conflicting proxy relation exists. " +
                            "Conflicting proxyIds: ${
                                existingProxyEntriesThatConflict.joinToString(", ") { it.proxyId.toString() }
                            }.",
                )
            }
        }

        private fun findConflictingProxies(
            existingProxies: List<CompanyProxyEntity>,
            candidateProxy: CompanyProxy<UUID>,
        ): List<Pair<CompanyProxyEntity, CompanyProxyEntity>> {
            val candidateCombinations = getAllFrameworkAndReportingPeriodCombinationsForAProxyEntry(candidateProxy).toSet()

            val expandedExistingCombinations = mutableListOf<Pair<CompanyProxyEntity, CompanyProxyEntity>>()

            existingProxies.forEach { existingProxy ->
                getAllFrameworkAndReportingPeriodCombinationsForAProxyEntry(
                    CompanyProxy(
                        proxiedCompanyId = existingProxy.proxiedCompanyId,
                        proxyCompanyId = existingProxy.proxyCompanyId,
                        framework = existingProxy.framework,
                        reportingPeriod = existingProxy.reportingPeriod,
                    ),
                ).forEach { expanded ->
                    expandedExistingCombinations.add(existingProxy to expanded)
                }
            }

            return expandedExistingCombinations.filter { (_, expandedCombination) ->
                expandedCombination.framework in candidateCombinations.map { it.framework } &&
                    expandedCombination.reportingPeriod in candidateCombinations.map { it.reportingPeriod }
            }
        }

        /**
         * Get all possible combinations of framework and reporting period for a proxy entry.
         * If either is null ("all"), all valid combinations are returned accordingly.
         */
        private fun getAllFrameworkAndReportingPeriodCombinationsForAProxyEntry(
            companyProxy: CompanyProxy<UUID>,
        ): List<CompanyProxyEntity> {
            val reportingPeriodRange = ValidationUtils.REPORTING_PERIOD_MINIMUM..ValidationUtils.REPORTING_PERIOD_MAXIMUM

            return when {
                companyProxy.framework == null && companyProxy.reportingPeriod == null -> {
                    reportingPeriodRange.flatMap { rp ->
                        DataType.values.map { fw ->
                            CompanyProxyEntity(
                                proxiedCompanyId = companyProxy.proxiedCompanyId,
                                proxyCompanyId = companyProxy.proxyCompanyId,
                                framework = fw.toString(),
                                reportingPeriod = rp.toString(),
                            )
                        }
                    }
                }
                companyProxy.framework != null && companyProxy.reportingPeriod == null -> {
                    reportingPeriodRange.map { rp ->
                        CompanyProxyEntity(
                            proxiedCompanyId = companyProxy.proxiedCompanyId,
                            proxyCompanyId = companyProxy.proxyCompanyId,
                            framework = companyProxy.framework,
                            reportingPeriod = rp.toString(),
                        )
                    }
                }
                companyProxy.framework == null && companyProxy.reportingPeriod != null -> {
                    DataType.values.map { fw ->
                        CompanyProxyEntity(
                            proxiedCompanyId = companyProxy.proxiedCompanyId,
                            proxyCompanyId = companyProxy.proxyCompanyId,
                            framework = fw.toString(),
                            reportingPeriod = companyProxy.reportingPeriod,
                        )
                    }
                }
                else -> {
                    listOf(
                        CompanyProxyEntity(
                            proxiedCompanyId = companyProxy.proxiedCompanyId,
                            proxyCompanyId = companyProxy.proxyCompanyId,
                            framework = companyProxy.framework,
                            reportingPeriod = companyProxy.reportingPeriod,
                        ),
                    )
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
                    "(proxiedCompanyId=${existing.proxiedCompanyId}, " +
                    "proxyCompanyId=${existing.proxyCompanyId}, " +
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
        ): StoredCompanyProxy {
            val existingProxiesForCompany =
                companyProxyRepository
                    .findAllByProxiedCompanyId(updatedCompanyProxy.proxiedCompanyId)
                    .filter { it.proxyId != proxyId }
            assertConflictingProxies(existingProxiesForCompany, updatedCompanyProxy)

            val existing = retrieveCompanyProxyEntityById(proxyId)
            existing.proxiedCompanyId = updatedCompanyProxy.proxiedCompanyId
            existing.proxyCompanyId = updatedCompanyProxy.proxyCompanyId
            existing.framework = updatedCompanyProxy.framework
            existing.reportingPeriod = updatedCompanyProxy.reportingPeriod

            return companyProxyRepository.save(existing).toStoredCompanyProxy()
        }
    }
