package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.model.proxies.StoredCompanyProxy
import org.dataland.datalandbackend.repositories.CompanyProxyRepository
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
        fun getCompanyProxyById(proxyId: UUID): StoredCompanyProxy =
            companyDataProxyRuleRepository.findByProxyId(proxyId)?.toStoredCompanyProxy()
                ?: throw ResourceNotFoundApiException(
                    summary = "Company proxy not found.",
                    message = "No company proxy rule exists for the proxyId=$proxyId.",
                )

        /**
         * Creates or replaces all proxy rules for a given (proxiedCompanyId, proxyCompanyId) pair.
         *
         * Semantics:
         *  - If both frameworks and reportingPeriods are empty or null:
         *      → single rule with framework = null, reportingPeriod = null
         *        meaning "all frameworks, all periods".
         *  - If frameworks is empty/null but reportingPeriods has values:
         *      → one rule per reporting period with framework = null.
         *  - If reportingPeriods is empty/null but frameworks has values:
         *      → one rule per framework with reportingPeriod = null.
         *  - If both lists have values:
         *      → full cross product of (framework, reportingPeriod).
         */
        @Transactional
        fun addProxyRelation(relation: CompanyProxy): CompanyProxyEntity {
            val proxiedCompanyId = relation.proxiedCompanyId
            val proxyCompanyId = relation.proxyCompanyId

            logger.info(
                "Adding proxy relation for proxiedCompanyId=$proxiedCompanyId, " +
                    "proxyCompanyId=$proxyCompanyId, " +
                    "framework='${relation.framework}', " +
                    "reportingPeriod='${relation.reportingPeriod}'",
            )

            // TODO: Check if that company full combination already exists and if yes post should not allow it.
            // TODO: Add check that this is not a contradiction. E.g. for a proxiedCompanyId=A,
            //  and fixed reportingPeriod and framework, there can not be more than one proxyCompany.

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

        /**
         * Returns all proxy rules for a given proxiedCompanyId as domain models.
         *
         * Semantics:
         *  - Each row in company_proxy_relations becomes one CompanyProxy.
         *  - framework = null → applies to all frameworks
         *  - reportingPeriod = null → applies to all reporting periods
         */
        @Transactional(readOnly = true)
        fun getProxyRelations(proxiedCompanyId: UUID): List<CompanyProxy> {
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
            proxyID: UUID,
            companyProxy: CompanyProxy,
        ): CompanyProxy = companyProxy
    }
