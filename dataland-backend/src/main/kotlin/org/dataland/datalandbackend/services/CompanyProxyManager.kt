package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyProxyEntity
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
         *
         * Checks for existing conflicting rules to avoid duplicates.
         *
         * @throws InvalidInputApiException if the validity interval is invalid, the parent company is itself
         * a child company, or a conflicting rule already exists.
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

            assertValidInterval(candidateProxy)
            assertParentIsNoChild(candidateProxy)
            logger.info("Checking for existing proxy relations to avoid duplicates...")

            val existingProxies = companyProxyRepository.findAllByChildCompanyId(candidateProxy.childCompanyId)
            assertNoConflictingProxiesExist(existingProxies, candidateProxy)

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

        private fun assertValidInterval(candidateProxy: CompanyProxy<UUID>) {
            val (from, to) = getReportingPeriodInterval(candidateProxy)
            if (from > to) {
                throw InvalidInputApiException(
                    summary = "Invalid validity interval",
                    message =
                        "validFromReportingPeriod (${candidateProxy.validFromReportingPeriod}) must not be " +
                            "after validToReportingPeriod (${candidateProxy.validToReportingPeriod}).",
                )
            }
        }

        private fun assertParentIsNoChild(candidateProxy: CompanyProxy<UUID>) {
            if (companyProxyRepository.existsByChildCompanyId(candidateProxy.parentCompanyId)) {
                throw InvalidInputApiException(
                    summary = "Parent company is a child company",
                    message =
                        "The parent company ${candidateProxy.parentCompanyId} is itself a child company of another proxy.",
                )
            }
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
            val (candidateFrom, candidateTo) = getReportingPeriodInterval(candidateProxy)

            return existingProxies.filter { existingProxy ->
                val (existingFrom, existingTo) =
                    getReportingPeriodInterval(existingProxy.validFromReportingPeriod, existingProxy.validToReportingPeriod)
                candidateFrom <= existingTo && existingFrom <= candidateTo
            }
        }

        private fun getReportingPeriodInterval(companyProxy: CompanyProxy<UUID>): Pair<Int, Int> =
            getReportingPeriodInterval(companyProxy.validFromReportingPeriod, companyProxy.validToReportingPeriod)

        /**
         * Get the numeric validity interval of a proxy entry.
         * A null validFrom is treated as the minimum and a null validTo as the maximum valid reporting period.
         */
        private fun getReportingPeriodInterval(
            validFromReportingPeriod: String?,
            validToReportingPeriod: String?,
        ): Pair<Int, Int> =
            Pair(
                validFromReportingPeriod?.toInt() ?: ValidationUtils.REPORTING_PERIOD_MINIMUM,
                validToReportingPeriod?.toInt() ?: ValidationUtils.REPORTING_PERIOD_MAXIMUM,
            )

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
         *
         * Checks for existing conflicting rules to avoid duplicates.
         *
         * @throws InvalidInputApiException if the validity interval is invalid, the parent company is itself
         * a child company, or a conflicting rule already exists.
         */
        @Transactional
        fun editCompanyProxy(
            proxyId: UUID,
            updatedCompanyProxy: CompanyProxy<UUID>,
            uploaderUserId: String,
        ): StoredCompanyProxy {
            assertValidInterval(updatedCompanyProxy)
            assertParentIsNoChild(updatedCompanyProxy)
            val existingProxiesForCompany =
                companyProxyRepository
                    .findAllByChildCompanyId(updatedCompanyProxy.childCompanyId)
                    .filter { it.proxyId != proxyId }
            assertNoConflictingProxiesExist(existingProxiesForCompany, updatedCompanyProxy)

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
