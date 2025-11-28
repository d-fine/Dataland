package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyProxyEntity
import org.dataland.datalandbackend.model.proxies.CompanyProxy
import org.dataland.datalandbackend.repositories.CompanyProxyRepository
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
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
        fun addProxyRelation(relation: CompanyProxy): List<CompanyProxyEntity> {
            val proxiedCompanyId = relation.proxiedCompanyId
            val proxyCompanyId = relation.proxyCompanyId

            logger.info(
                "Adding relations for proxiedCompanyId=$proxiedCompanyId, proxyCompanyId=$proxyCompanyId",
            )

            // TODO: Check if that company pair already exists and if yes post should not allow it.
            // TODO: Add check that this is not a contradiction. E.g. for a proxiedCompanyId=A,
            //  and fixed reportingPeriod and framework, there can not be more than one proxyCompany.
            val frameworks = relation.frameworks.orEmpty()
            val reportingPeriods = relation.reportingPeriods.orEmpty()

            // TODO: Check if the post endpoint should be called with a list for reporting periods and frameworks and
            // translate it internally or if it only should only accept a single reporting period or framework.
            val entities: List<CompanyProxyEntity> =
                when {
                    frameworks.isEmpty() && reportingPeriods.isEmpty() -> {
                        listOf(
                            CompanyProxyEntity(
                                proxiedCompanyId = proxiedCompanyId,
                                proxyCompanyId = proxyCompanyId,
                                framework = null,
                                reportingPeriod = null,
                            ),
                        )
                    }

                    frameworks.isEmpty() -> {
                        reportingPeriods.map { period ->
                            CompanyProxyEntity(
                                proxiedCompanyId = proxiedCompanyId,
                                proxyCompanyId = proxyCompanyId,
                                framework = null,
                                reportingPeriod = period,
                            )
                        }
                    }

                    reportingPeriods.isEmpty() -> {
                        frameworks.map { framework ->
                            CompanyProxyEntity(
                                proxiedCompanyId = proxiedCompanyId,
                                proxyCompanyId = proxyCompanyId,
                                framework = framework,
                                reportingPeriod = null,
                            )
                        }
                    }

                    else -> {
                        frameworks.flatMap { framework ->
                            reportingPeriods.map { period ->
                                CompanyProxyEntity(
                                    proxiedCompanyId = proxiedCompanyId,
                                    proxyCompanyId = proxyCompanyId,
                                    framework = framework,
                                    reportingPeriod = period,
                                )
                            }
                        }
                    }
                }

            val savedRelations = companyDataProxyRuleRepository.saveAll(entities)
            logger.info(
                "Saved ${savedRelations.size} proxy rules for proxiedCompanyId=$proxiedCompanyId, proxyCompanyId=$proxyCompanyId",
            )
            return savedRelations
        }

        /**
         * Returns the aggregated proxy rules for a given (proxiedCompanyId, proxyCompanyId) pair
         * as a single API model.
         *
         * Aggregation semantics:
         *  - If any stored rule has framework = null, the result frameworks list is interpreted as "all"
         *    (returned as empty list in the DTO, matching the documented semantics).
         *  - If any stored rule has reportingPeriod = null, the result reportingPeriods list is interpreted
         *    as "all" (returned as empty list).
         */
        @Transactional(readOnly = true)
        fun getProxyRelation(proxiedCompanyId: UUID): List<CompanyProxy> {
            // TODO: change function name to plural
            val entities =
                companyDataProxyRuleRepository
                    .findAllByProxiedCompanyId(proxiedCompanyId)

            if (entities.isEmpty()) {
                throw InvalidInputApiException(
                    "No proxy rules found for proxiedCompanyId=$proxiedCompanyId",
                    message = "ThisStillNeedsAMessage",
                )
            }

            return entities
                .groupBy { it.proxyCompanyId }
                .map { (proxyCompanyId, rows) ->
                    val hasAllFrameworks = rows.any { it.framework == null }
                    val hasAllPeriods = rows.any { it.reportingPeriod == null }

                    val distinctFrameworks =
                        rows.mapNotNull { it.framework }.distinct().sorted()
                    val distinctPeriods =
                        rows.mapNotNull { it.reportingPeriod }.distinct().sorted()

                    val dtoFrameworks =
                        if (hasAllFrameworks) emptyList() else distinctFrameworks

                    val dtoReportingPeriods =
                        if (hasAllPeriods) emptyList() else distinctPeriods

                    CompanyProxy(
                        proxiedCompanyId = proxiedCompanyId,
                        proxyCompanyId = proxyCompanyId,
                        frameworks = dtoFrameworks,
                        reportingPeriods = dtoReportingPeriods,
                    )
                }
        }

        /**
         * Deletes all proxy rules for a given (proxiedCompanyId, proxyCompanyId) pair.
         *
         * If no rules exist for this pair, an InvalidInputApiException is thrown.
         */
        @Transactional
        fun deleteProxyRelation(technicalId: UUID): CompanyProxyEntity {
            val existing =
                companyDataProxyRuleRepository
                    .findById(technicalId)
                    .orElseThrow {
                        InvalidInputApiException(
                            "No proxy rule found for id=$technicalId",
                            message = "No proxy rule exists for the specified id.",
                        )
                    }

            logger.info(
                "Deleting proxy rule with id=$technicalId " +
                    "(proxiedCompanyId=${existing.proxiedCompanyId}, " +
                    "proxyCompanyId=${existing.proxyCompanyId}, " +
                    "framework=${existing.framework}, reportingPeriod=${existing.reportingPeriod})",
            )

            companyDataProxyRuleRepository.delete(existing)

            return existing
        }
    }
