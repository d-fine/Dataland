package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.CompanyIdentifierEntityId
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.exceptions.DuplicateIdentifierApiException
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
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
 * @param companyIdentifierRepositoryInterface JPA repository for company identifiers
 */
@Service
class CompanyAlterationManager(
    @Autowired private val companyRepository: StoredCompanyRepository,
    @Autowired private val companyIdentifierRepositoryInterface: CompanyIdentifierRepository,
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
            companyContactDetails = companyInformation.companyContactDetails,
            companyLegalForm = companyInformation.companyLegalForm,
            headquarters = companyInformation.headquarters,
            headquartersPostalCode = companyInformation.headquartersPostalCode,
            sector = companyInformation.sector,
            sectorCodeWz = companyInformation.sectorCodeWz,
            identifiers = mutableListOf(),
            dataRegisteredByDataland = mutableListOf(),
            countryCode = companyInformation.countryCode,
            isTeaserCompany = companyInformation.isTeaserCompany ?: false,
            website = companyInformation.website,
            parentCompanyLei = companyInformation.parentCompanyLei,
        )

        return companyRepository.save(newCompanyEntity)
    }

    private fun assertNoDuplicateIdentifiersExist(identifierMap: Map<IdentifierType, List<String>>) {
        val duplicateIdentifiers = companyIdentifierRepositoryInterface.findAllById(
            identifierMap.flatMap { identifierPair ->
                identifierPair.value.map {
                    CompanyIdentifierEntityId(
                        identifierType = identifierPair.key,
                        identifierValue = it,
                    )
                }
            },
        )

        if (duplicateIdentifiers.isNotEmpty()) {
            throw DuplicateIdentifierApiException(duplicateIdentifiers)
        }
    }

    private fun createAndAssociateIdentifiers(
        savedCompanyEntity: StoredCompanyEntity,
        identifierMap: Map<IdentifierType, List<String>>,
    ): List<CompanyIdentifierEntity> {
        assertNoDuplicateIdentifiersExist(identifierMap)

        val newIdentifiers = identifierMap.flatMap { identifierPair ->
            identifierPair.value.map {
                CompanyIdentifierEntity(
                    identifierType = identifierPair.key, identifierValue = it,
                    company = savedCompanyEntity, isNew = true,
                )
            }
        }
        try {
            return companyIdentifierRepositoryInterface.saveAllAndFlush(newIdentifiers).toList()
        } catch (ex: DataIntegrityViolationException) {
            val cause = ex.cause
            if (cause is ConstraintViolationException && cause.constraintName == "company_identifiers_pkey") {
                // Cannot access the list of duplicate identifiers here because of hibernate caching.
                throw DuplicateIdentifierApiException(null)
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
        companyIdentifierRepositoryInterface.deleteAllByCompanyAndIdentifierType(companyEntity, identifierType)
        createAndAssociateIdentifiers(companyEntity, mapOf(identifierType to newIdentifiers))
    }

    /**
     * Method to patch the information of a company.
     * @param companyId the id of the company to patch
     * @param patch the patch to apply to the company
     * @return the updated company information object
     */
    @Suppress("CyclomaticComplexMethod")
    @Transactional
    fun patchCompany(companyId: String, patch: CompanyInformationPatch): StoredCompanyEntity {
        val companyEntity = companyQueryManager.getCompanyById(companyId)
        logger.info("Patching Company ${companyEntity.companyName} with ID $companyId")
        patch.companyName?.let { companyEntity.companyName = it }
        patch.companyContactDetails?.let { companyEntity.companyContactDetails = it }
        patch.companyLegalForm?.let { companyEntity.companyLegalForm = it }
        patch.headquarters?.let { companyEntity.headquarters = it }
        patch.headquartersPostalCode?.let { companyEntity.headquartersPostalCode = it }
        patch.sector?.let { companyEntity.sector = it }
        patch.sectorCodeWz?.let { companyEntity.sectorCodeWz = it }
        patch.countryCode?.let { companyEntity.countryCode = it }
        patch.website?.let { companyEntity.website = it }
        patch.isTeaserCompany?.let { companyEntity.isTeaserCompany = it }
        patch.parentCompanyLei?.let { companyEntity.parentCompanyLei = it }

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
        val storedCompanyEntity = companyQueryManager.getCompanyById(companyId)
        logger.info("Updating Company ${storedCompanyEntity.companyName} with ID $companyId")
        storedCompanyEntity.companyName = companyInformation.companyName
        storedCompanyEntity.companyContactDetails = companyInformation.companyContactDetails
        storedCompanyEntity.companyLegalForm = companyInformation.companyLegalForm
        storedCompanyEntity.headquarters = companyInformation.headquarters
        storedCompanyEntity.headquartersPostalCode = companyInformation.headquartersPostalCode
        storedCompanyEntity.sector = companyInformation.sector
        storedCompanyEntity.sectorCodeWz = companyInformation.sectorCodeWz
        storedCompanyEntity.countryCode = companyInformation.countryCode
        storedCompanyEntity.website = companyInformation.website
        storedCompanyEntity.isTeaserCompany = companyInformation.isTeaserCompany ?: false
        storedCompanyEntity.parentCompanyLei = companyInformation.parentCompanyLei
        storedCompanyEntity.companyAlternativeNames = companyInformation.companyAlternativeNames
        companyIdentifierRepositoryInterface.deleteAllByCompany(
            storedCompanyEntity,
        )
        createAndAssociateIdentifiers(storedCompanyEntity, companyInformation.identifiers)
        return companyRepository.save(storedCompanyEntity)
    }
}
