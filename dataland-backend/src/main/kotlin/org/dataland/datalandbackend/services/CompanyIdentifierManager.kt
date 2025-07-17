package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntityId
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.dataland.datalandbackend.utils.CompanyIdentifierUtils
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service for managing company identifiers, including ISIN and LEI.
 * Provides methods to search for identifiers and retrieve company IDs based on identifiers.
 *
 * @param companyIdentifierRepository Repository for accessing company identifiers.
 * @param isinLeiRepository Repository for accessing ISIN-LEI entities.
 */
@Service
class CompanyIdentifierManager(
    @Autowired private val companyIdentifierRepository: CompanyIdentifierRepository,
    @Autowired private val isinLeiRepository: IsinLeiRepository,
) {
    /**
     * Searches for a company identifier based on the identifier type and value.
     *
     * @param identifierType The type of identifier to search for (e.g., ISIN, LEI).
     * @param identifier The value of the identifier to search for.
     * @throws ResourceNotFoundApiException if the company identifier is not found.
     */
    fun searchForCompanyIdentifier(
        identifierType: IdentifierType,
        identifier: String,
    ) {
        if (identifierType != IdentifierType.Isin) {
            companyIdentifierRepository.getReferenceById(
                CompanyIdentifierEntityId(
                    identifier,
                    identifierType,
                ),
            )
        } else {
            isinLeiRepository.findByIsin(identifier)
                ?: throw ResourceNotFoundApiException(
                    CompanyIdentifierUtils.COMPANY_NOT_FOUND_SUMMARY,
                    CompanyIdentifierUtils.companyNotFoundMessage(identifierType, identifier),
                )
        }
    }

    /**
     * Retrieves the company ID associated with a given identifier type and value.
     *
     * @param identifierType The type of identifier (e.g., ISIN, LEI).
     * @param identifier The value of the identifier.
     * @return The company ID associated with the identifier.
     * @throws ResourceNotFoundApiException if the company is not found for the given identifier.
     */
    fun getCompanyIdByIdentifier(
        identifierType: IdentifierType,
        identifier: String,
    ): String {
        if (identifierType != IdentifierType.Isin) {
            return companyIdentifierRepository
                .getReferenceById(
                    CompanyIdentifierEntityId(identifier, identifierType),
                ).company!!
                .companyId
        }

        return isinLeiRepository.findByIsin(identifier)?.company?.companyId
            ?: throw ResourceNotFoundApiException(
                CompanyIdentifierUtils.COMPANY_NOT_FOUND_SUMMARY,
                CompanyIdentifierUtils.companyNotFoundMessage(identifierType, identifier),
            )
    }
}
