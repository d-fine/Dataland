package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.api.COMPANY_SEARCH_STRING_MIN_LENGTH
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.companies.CompanyIdentifierValidationResult
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.utils.CompanyUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of common read-only queries against company data that include company identifiers.
 * @param companyRepository  JPA for company data
 */
@Service("CompanyIdentifierManager")
class CompanyIdentifierManager
    @Autowired
    constructor(
        private val companyIdentifierRepository: CompanyIdentifierRepository,
        private val companyRepository: StoredCompanyRepository,
        private val companyUtils: CompanyUtils,
    ) {
        /**
         * For a collection of company IDs return a map associating each ID with the corresponding BasicCompanyInformation.
         */
        @Transactional(readOnly = true)
        fun getBasicCompanyInformationByIds(companyIds: Collection<String>): Map<String, BasicCompanyInformation?> {
            val storedCompanies = companyRepository.findAllById(companyIds).associateBy { it.companyId }
            val companyLeis =
                companyIdentifierRepository
                    .findCompanyIdentifierEntitiesByCompanyInAndIdentifierTypeIs(
                        storedCompanies.values,
                        IdentifierType.Lei,
                    ).associateBy { it.company?.companyId }

            return companyIds.associateWith { companyId ->
                storedCompanies[companyId]?.let {
                    BasicCompanyInformation(
                        companyId = companyId,
                        companyName = it.companyName,
                        headquarters = it.headquarters,
                        countryCode = it.countryCode,
                        sector = it.sector,
                        lei = companyLeis[companyId]?.identifierValue,
                    )
                }
            }
        }

        /**
         * Build a validation result using an identifier and company information
         * @param identifier the identifier used to obtain the company information
         * @param storedCompanyEntity the entity containing the company information
         */
        private fun buildCompanyIdentifierValidationResult(
            identifier: String,
            storedCompanyEntity: StoredCompanyEntity,
        ): CompanyIdentifierValidationResult =
            CompanyIdentifierValidationResult(
                identifier = identifier,
                companyId = storedCompanyEntity.companyId,
                companyName = storedCompanyEntity.companyName,
                headquarters = storedCompanyEntity.headquarters,
                countryCode = storedCompanyEntity.countryCode,
                sector = storedCompanyEntity.sector,
                lei =
                    storedCompanyEntity.identifiers
                        .firstOrNull { it.identifierType == IdentifierType.Lei }
                        ?.identifierValue,
            )

        /**
         * Retrieve a validation result for a single company identifier
         * @param identifier a company identifier to validate
         */
        private fun getCompanyIdentifierValidationResult(identifier: String): CompanyIdentifierValidationResult =
            if (identifier.length < COMPANY_SEARCH_STRING_MIN_LENGTH) {
                CompanyIdentifierValidationResult(identifier)
            } else if (companyUtils.checkCompanyIdExists(identifier)) {
                buildCompanyIdentifierValidationResult(identifier, companyUtils.getCompanyByIdAndAssertExistence(identifier))
            } else {
                companyIdentifierRepository.getFirstByIdentifierValueIs(identifier)?.company?.let {
                    buildCompanyIdentifierValidationResult(identifier, it)
                } ?: CompanyIdentifierValidationResult(identifier)
            }

        /**
         * A method to validate if a given list of identifiers corresponds to a company in Dataland.
         * @param identifiers list of identifiers to validate
         * @return list of validation results
         */
        @Transactional(readOnly = true)
        fun validateCompanyIdentifiers(identifiers: List<String>): List<CompanyIdentifierValidationResult> =
            identifiers.map { getCompanyIdentifierValidationResult(it.trim()) }.distinctBy {
                it.companyInformation?.companyId ?: it.identifier
            }
    }
