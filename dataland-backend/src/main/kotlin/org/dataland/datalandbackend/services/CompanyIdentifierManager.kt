package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntityId
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.dataland.datalandbackend.utils.CompanyIdentifierUtils
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CompanyIdentifierManager(
    @Autowired private val companyIdentifierRepository: CompanyIdentifierRepository,
    @Autowired private val isinLeiRepository: IsinLeiRepository,
) {
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

        return isinLeiRepository.findByIsin(identifier)?.companyId
            ?: throw ResourceNotFoundApiException(
                CompanyIdentifierUtils.COMPANY_NOT_FOUND_SUMMARY,
                CompanyIdentifierUtils.companyNotFoundMessage(identifierType, identifier),
            )
    }
}
