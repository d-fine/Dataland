package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.exceptions.DuplicateIdentifierApiException
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.IsinLeiRepository
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
 * @param storedCompanyRepository  JPA for company data
 * @param companyIdentifierRepositoryInterface JPA repository for company identifiers
 */
@Service
class CompanyAlterationManager
    @Autowired
    constructor(
        private val storedCompanyRepository: StoredCompanyRepository,
        private val companyIdentifierRepositoryInterface: CompanyIdentifierRepository,
        private val companyQueryManager: CompanyQueryManager,
        private val isinLeiRepository: IsinLeiRepository,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        private fun createStoredCompanyEntityWithoutForeignReferences(
            companyId: String,
            companyInformation: CompanyInformation,
        ): StoredCompanyEntity {
            val newCompanyEntity =
                StoredCompanyEntity(
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

            return storedCompanyRepository.save(newCompanyEntity)
        }

        /**
         * Asserts that none of the identifiers in [identifiers] is used outside of [storedCompanyEntity]
         * @param storedCompanyEntity the company entity to be excluded from the check
         * @param identifiers all identifiers to be checked given in a map format
         */
        private fun assertNoDuplicateIdentifiersExist(
            storedCompanyEntity: StoredCompanyEntity,
            identifiers: Map<IdentifierType, List<String>>,
        ) {
            val duplicates = mutableListOf<CompanyIdentifierEntity>()
            identifiers.forEach { identifierType, identifiers ->
                when (identifierType) {
                    IdentifierType.Isin -> {
                        duplicates.addAll(
                            isinLeiRepository
                                .findAllByIsinInAndCompanyIsNot(
                                    identifiers, storedCompanyEntity,
                                ).map { it.toCompanyIdentifierEntity() },
                        )
                    }
                    else -> {
                        duplicates.addAll(
                            companyIdentifierRepositoryInterface
                                .findByIdentifierTypeIsAndIdentifierValueInAndCompanyIsNot(
                                    identifierType, identifiers, storedCompanyEntity,
                                ),
                        )
                    }
                }
            }
            if (duplicates.isNotEmpty()) {
                throw DuplicateIdentifierApiException(duplicates)
            }
        }

        private fun createAndAssociateNonIsinIdentifiers(
            storedCompanyEntity: StoredCompanyEntity,
            identifierMap: Map<IdentifierType, List<String>>,
        ): List<CompanyIdentifierEntity> {
            val newNonIsinIdentifiers =
                identifierMap
                    .flatMap { identifierPair ->
                        identifierPair.value.map {
                            CompanyIdentifierEntity(
                                identifierType = identifierPair.key, identifierValue = it,
                                company = storedCompanyEntity, isNew = true,
                            )
                        }
                    }.distinct()
                    .filter { it.identifierType != IdentifierType.Isin }

            try {
                return companyIdentifierRepositoryInterface
                    .saveAllAndFlush(newNonIsinIdentifiers)
                    .toList()
            } catch (dataIntegrityViolationException: DataIntegrityViolationException) {
                val cause = dataIntegrityViolationException.cause
                if (cause is ConstraintViolationException && cause.constraintName == "company_identifiers_pkey") {
                    // Cannot access the list of duplicate identifiers here because of hibernate caching.
                    throw DuplicateIdentifierApiException(null)
                }
                throw dataIntegrityViolationException
            }
        }

        private fun createAndAssociateIsinIdentifiers(
            storedCompanyEntity: StoredCompanyEntity,
            identifiers: Map<IdentifierType, List<String>>,
        ) {
            val isins = identifiers[IdentifierType.Isin]
            val lei = identifiers[IdentifierType.Lei]?.firstOrNull()
            if (isins.isNullOrEmpty()) return

            isinLeiRepository.saveAllAndFlush(
                isins.map { isin ->
                    IsinLeiEntity(
                        company = storedCompanyEntity,
                        isin = isin,
                        lei = lei,
                    )
                },
            )
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
            assertNoDuplicateIdentifiersExist(storedCompanyEntity = savedCompany, identifiers = companyInformation.identifiers)
            val nonIsinIdentifiers = createAndAssociateNonIsinIdentifiers(savedCompany, companyInformation.identifiers)
            createAndAssociateIsinIdentifiers(savedCompany, companyInformation.identifiers)
            savedCompany.identifiers = nonIsinIdentifiers.toMutableList()
            logger.info("Company ${companyInformation.companyName} with ID $companyId saved to database.")
            return savedCompany
        }

        /**
         * Method to patch the information of a company.
         * @param companyId the id of the company to patch
         * @param patch the patch to apply to the company
         * @return the updated company information object
         */
        @Transactional
        fun patchCompany(
            companyId: String,
            patch: CompanyInformationPatch,
        ): StoredCompanyEntity {
            val patchedIdentifiers = patch.identifiers ?: emptyMap()
            val storedCompanyEntity = companyQueryManager.getCompanyById(companyId)
            assertNoDuplicateIdentifiersExist(storedCompanyEntity = storedCompanyEntity, identifiers = patchedIdentifiers)

            logger.info("Patching Company ${storedCompanyEntity.companyName} with ID $companyId")
            storedCompanyEntity.applyPatchWithoutIdentifiers(patch)
            updateNonIsinIdentifers(storedCompanyEntity, patchedIdentifiers)

            if (patchedIdentifiers[IdentifierType.Isin] != null) {
                replaceIsinLeiEntities(
                    storedCompanyEntity = storedCompanyEntity,
                    newIsins = patchedIdentifiers[IdentifierType.Isin],
                    lei = storedCompanyEntity.identifiers.find { it.identifierType == IdentifierType.Lei }?.identifierValue,
                )
            }

            return storedCompanyRepository.save(storedCompanyEntity)
        }

        /**
         * Method to put the information of a company.
         * @param companyId the id of the company to patch
         * @param companyInformation denotes information of the company
         * @return the updated company information object
         */
        @Transactional
        fun putCompany(
            companyId: String,
            companyInformation: CompanyInformation,
        ): StoredCompanyEntity {
            val storedCompanyEntity = companyQueryManager.getCompanyById(companyId)
            assertNoDuplicateIdentifiersExist(storedCompanyEntity = storedCompanyEntity, identifiers = companyInformation.identifiers)
            logger.info("Updating Company ${storedCompanyEntity.companyName} with ID $companyId")

            storedCompanyEntity.applyPutWithoutIdentifiers(companyInformation)

            companyIdentifierRepositoryInterface.deleteAllByCompany(storedCompanyEntity)
            storedCompanyEntity.identifiers = mutableListOf()

            updateNonIsinIdentifers(storedCompanyEntity, companyInformation.identifiers)
            replaceIsinLeiEntities(
                storedCompanyEntity = storedCompanyEntity,
                newIsins = companyInformation.identifiers[IdentifierType.Isin],
                lei = companyInformation.identifiers[IdentifierType.Lei]?.firstOrNull(),
            )

            return storedCompanyRepository.save(storedCompanyEntity)
        }

        /**
         * Replaces the non-ISIN company identifiers associated with [storedCompanyEntity] for the given [identifierType].
         * If [newIdentifiers] is empty, the existing identifiers of that type are deleted without replacement.
         *
         * @param storedCompanyEntity the company entity whose identifiers are to be replaced
         * @param identifierType the type of identifier to replace
         * @param newIdentifiers the new identifiers to associate with the company; if empty, existing identifiers are deleted
         * @return a list of newly created [CompanyIdentifierEntity] instances for non-ISIN identifiers
         */
        private fun replaceNonIsinCompanyIdentifiers(
            storedCompanyEntity: StoredCompanyEntity,
            identifierType: IdentifierType,
            newIdentifiers: List<String>,
        ): List<CompanyIdentifierEntity> {
            if (identifierType == IdentifierType.Isin) return emptyList()
            companyIdentifierRepositoryInterface.deleteAllByCompanyAndIdentifierType(
                storedCompanyEntity,
                identifierType,
            )
            return createAndAssociateNonIsinIdentifiers(storedCompanyEntity, mapOf(identifierType to newIdentifiers))
        }

        /**
         * Replaces the ISIN-LEI entities associated with [storedCompanyEntity]. If [newIsins] is null or empty,
         * the existing ISIN-LEI entities are deleted without replacement.
         *
         * @param storedCompanyEntity the company entity whose ISIN-LEI entities are to be replaced
         * @param newIsins the new ISINs to associate with the company; if null or empty, existing entities are deleted
         * @param lei the LEI to associate with the new ISINs; if null or empty, no LEI is associated
         */
        private fun replaceIsinLeiEntities(
            storedCompanyEntity: StoredCompanyEntity,
            newIsins: List<String>?,
            lei: String?,
        ) {
            isinLeiRepository.deleteAllByCompany(storedCompanyEntity)
            if (newIsins.isNullOrEmpty()) return
            createAndAssociateIsinIdentifiers(
                storedCompanyEntity = storedCompanyEntity,
                identifiers =
                    mapOf(
                        IdentifierType.Isin to newIsins,
                        IdentifierType.Lei to if (lei.isNullOrEmpty()) emptyList() else listOf(lei),
                    ),
            )
        }

        /**
         * Updates the non-ISIN identifiers of the given [storedCompanyEntity] based on the provided [identifiers] map.
         * Existing identifiers of each type are removed before adding the new ones. The in memory entity is updated as well.
         *
         * @param storedCompanyEntity the company entity whose identifiers are to be updated
         * @param identifiers a map of identifier types to their new values
         */
        private fun updateNonIsinIdentifers(
            storedCompanyEntity: StoredCompanyEntity,
            identifiers: Map<IdentifierType, List<String>>,
        ) {
            identifiers.forEach { identifierType, identifierValues ->
                if (identifierType == IdentifierType.Isin) return@forEach
                storedCompanyEntity.identifiers.removeIf { it.identifierType == identifierType }
                storedCompanyEntity.identifiers.addAll(
                    replaceNonIsinCompanyIdentifiers(
                        storedCompanyEntity = storedCompanyEntity,
                        identifierType = identifierType,
                        newIdentifiers = identifierValues.distinct(),
                    ),
                )
            }
        }
    }
