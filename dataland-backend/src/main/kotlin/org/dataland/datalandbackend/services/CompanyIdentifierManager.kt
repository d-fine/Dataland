package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntityId
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CompanyIdentifierManager(
    @Autowired private val companyIdentifierRepository: CompanyIdentifierRepository,
    @Autowired private val isinLeiRepository: IsinLeiRepository,
) {
    fun getCompanyIdentifierReference(
        identifierType: IdentifierType,
        identifier: String,
    ) {
        when (identifierType) {
            IdentifierType.Isin -> isinLeiRepository.getReferenceById(identifier)
            else ->
                companyIdentifierRepository.getReferenceById(
                    CompanyIdentifierEntityId(
                        identifier,
                        identifierType,
                    ),
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

        val isinLeiPair = isinLeiRepository.getReferenceById(identifier)
        return companyIdentifierRepository
            .getReferenceById(
                CompanyIdentifierEntityId(isinLeiPair.lei, IdentifierType.Lei),
            ).company!!
            .companyId
    }
}
