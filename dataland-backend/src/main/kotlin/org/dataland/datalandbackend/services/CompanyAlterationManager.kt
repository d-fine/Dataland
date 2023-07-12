package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.CompanyInformationPatch
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of a company manager for Dataland
 * @param companyRepository  JPA for company data
 * @param companyIdentifierRepository JPA repository for company identifiers
 */
@Service
class CompanyAlterationManager(
    @Autowired private val companyRepository: StoredCompanyRepository,
    @Autowired private val companyIdentifierRepository: CompanyIdentifierRepository,
    @Autowired private val companyQueryManager: CompanyQueryManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun createStoredCompanyEntityWithoutForeignReferences(
        companyId: String,
        companyInformation: CompanyInformation,
    ): StoredCompanyEntity {
        val newCompanyEntity = StoredCompanyEntity(
            companyId = companyId,
            companyName = companyInformation.companyName,
            companyAlternativeNames = companyInformation.companyAlternativeNames,
            companyLegalForm = companyInformation.companyLegalForm,
            headquarters = companyInformation.headquarters,
            headquartersPostalCode = companyInformation.headquartersPostalCode,
            sector = companyInformation.sector,
            identifiers = mutableListOf(),
            dataRegisteredByDataland = mutableListOf(),
            countryCode = companyInformation.countryCode,
            isTeaserCompany = companyInformation.isTeaserCompany,
            website = companyInformation.website,
        )

        return companyRepository.save(newCompanyEntity)
    }

    private fun createAndAssociateIdentifiers(
        savedCompanyEntity: StoredCompanyEntity,
        identifierMap: Map<IdentifierType, List<String>>,
    ): List<CompanyIdentifierEntity> {
        val newIdentifiers = identifierMap.flatMap { identifierPair ->
            identifierPair.value.map {
                CompanyIdentifierEntity(
                    identifierType = identifierPair.key, identifierValue = it,
                    company = savedCompanyEntity, isNew = true,
                )
            }
        }
        try {
            return companyIdentifierRepository.saveAllAndFlush(newIdentifiers).toList()
        } catch (ex: DataIntegrityViolationException) {
            val cause = ex.cause
            if (cause is ConstraintViolationException && cause.constraintName == "company_identifiers_pkey") {
                throw InvalidInputApiException(
                    "Company identifier already used",
                    "Could not insert company as one company identifier is already used to identify another company",
                )
            }
            throw ex
        }
    }

    /**
     * Method to add a company
     * @param companyInformation denotes information of the company
     * @return information of the newly created entry in the company data store of Dataland,
     * including the generated company ID
     */
    @Transactional(rollbackFor = [InvalidInputApiException::class])
    fun addCompany(companyInformation: CompanyInformation): StoredCompanyEntity {
        val companyId = IdUtils.generateUUID()
        logger.info("Creating Company ${companyInformation.companyName} with ID $companyId")
        val savedCompany = createStoredCompanyEntityWithoutForeignReferences(companyId, companyInformation)
        val identifiers = createAndAssociateIdentifiers(savedCompany, companyInformation.identifiers)
        savedCompany.identifiers = identifiers.toMutableList()
        logger.info("Company ${companyInformation.companyName} with ID $companyId saved to database.")
        return savedCompany
    }

    private fun replaceCompanyIdentifiers(
        companyEntity: StoredCompanyEntity,
        identifierType: IdentifierType,
        newIdentifiers: List<String>,
    ) {
        companyIdentifierRepository.deleteAllByCompanyAndIdentifierType(companyEntity, identifierType)
        createAndAssociateIdentifiers(companyEntity, mapOf(identifierType to newIdentifiers))
    }

    /**
     * Method to patch the information of a company.
     * @param companyId the id of the company to patch
     * @param patch the patch to apply to the company
     * @return the updated company information object
     */
    @Transactional
    fun patchCompany(companyId: String, patch: CompanyInformationPatch): StoredCompanyEntity {
        val companyEntity = companyQueryManager.getCompanyById(companyId)
        logger.info("Patching Company ${companyEntity.companyName} with ID $companyId")
        patch.companyName?.let { companyEntity.companyName = it }
        patch.companyLegalForm?.let { companyEntity.companyLegalForm = it }
        patch.headquarters?.let { companyEntity.headquarters = it }
        patch.headquartersPostalCode?.let { companyEntity.headquartersPostalCode = it }
        patch.sector?.let { companyEntity.sector = it }
        patch.countryCode?.let { companyEntity.countryCode = it }
        patch.website?.let { companyEntity.website = it }
        patch.isTeaserCompany?.let { companyEntity.isTeaserCompany = it }

        if (patch.companyAlternativeNames != null) {
            companyEntity.companyAlternativeNames = patch.companyAlternativeNames
        }

        if (patch.identifiers != null) {
            for (keypair in patch.identifiers) {
                replaceCompanyIdentifiers(
                    companyEntity = companyEntity,
                    identifierType = keypair.key,
                    newIdentifiers = keypair.value,
                )
            }
        }

        return companyRepository.save(companyEntity)
    }

    /**
     * Method to put the information of a company.
     * @param companyId the id of the company to patch
     * @param companyInformation denotes information of the company
     * @return the updated company information object
     */
    @Transactional
    fun putCompany(companyId: String, companyInformation: CompanyInformation): StoredCompanyEntity {
        val companyEntity = companyQueryManager.getCompanyById(companyId)
        logger.info("Updating Company ${companyEntity.companyName} with ID $companyId")
        companyEntity.companyName = companyInformation.companyName
        companyEntity.companyLegalForm = companyInformation.companyLegalForm
        companyEntity.headquarters = companyInformation.headquarters
        companyEntity.headquartersPostalCode = companyInformation.headquartersPostalCode
        companyEntity.sector = companyInformation.sector
        companyEntity.countryCode = companyInformation.countryCode
        companyEntity.website = companyInformation.website
        companyEntity.isTeaserCompany = companyInformation.isTeaserCompany
        companyEntity.companyAlternativeNames = companyInformation.companyAlternativeNames

            for (keypair in companyInformation.identifiers) {
                replaceCompanyIdentifiers(
                        companyEntity = companyEntity,
                        identifierType = keypair.key,
                        newIdentifiers = keypair.value,
                )
            }

        return companyRepository.save(companyEntity)
    }
}
