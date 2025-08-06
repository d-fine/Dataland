package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.CompanyIdentifierEntityId
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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
            val storedCompanyEntity = companyQueryManager.getCompanyById(companyId)
            logger.info("Patching Company ${storedCompanyEntity.companyName} with ID $companyId")

            applyPatch(storedCompanyEntity, patch)

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
            logger.info("Replacing Company ${storedCompanyEntity.companyName} with ID $companyId")

            applyPut(storedCompanyEntity, companyInformation)

            return storedCompanyRepository.save(storedCompanyEntity)
        }

        private fun applyPatch(
            storedCompanyEntity: StoredCompanyEntity,
            patch: CompanyInformationPatch,
        ) {
            patch.companyName?.let { storedCompanyEntity.companyName = it }
            patch.companyAlternativeNames?.let { storedCompanyEntity.companyAlternativeNames = it.toMutableList() }
            patch.companyContactDetails?.let { storedCompanyEntity.companyContactDetails = it.toMutableList() }
            patch.companyLegalForm?.let { storedCompanyEntity.companyLegalForm = it }
            patch.headquarters?.let { storedCompanyEntity.headquarters = it }
            patch.headquartersPostalCode?.let { storedCompanyEntity.headquartersPostalCode = it }
            patch.sector?.let { storedCompanyEntity.sector = it }
            patch.sectorCodeWz?.let { storedCompanyEntity.sectorCodeWz = it }
            patch.countryCode?.let { storedCompanyEntity.countryCode = it }
            patch.website?.let { storedCompanyEntity.website = it }
            patch.isTeaserCompany?.let { storedCompanyEntity.isTeaserCompany = it }
            patch.parentCompanyLei?.let { storedCompanyEntity.parentCompanyLei = it }

            val patchedIdentifiers = patch.identifiers ?: emptyMap()
            patchIdentifiers(storedCompanyEntity, patchedIdentifiers)
        }

        private fun patchIdentifiers(
            storedCompanyEntity: StoredCompanyEntity,
            patchedIdentifiers: Map<IdentifierType, List<String>>,
        ) {
            updateNonIsinIdentifiers(storedCompanyEntity, patchedIdentifiers)

            if (patchedIdentifiers[IdentifierType.Isin] != null) {
                replaceIsinIdentifiers(
                    storedCompanyEntity = storedCompanyEntity,
                    identifierMap = patchedIdentifiers,
                )
            }
        }

        private fun applyPut(
            storedCompanyEntity: StoredCompanyEntity,
            put: CompanyInformation,
        ) {
            storedCompanyEntity.companyName = put.companyName
            storedCompanyEntity.companyAlternativeNames = put.companyAlternativeNames?.toMutableList()
            storedCompanyEntity.companyContactDetails = put.companyContactDetails?.toMutableList()
            storedCompanyEntity.companyLegalForm = put.companyLegalForm
            storedCompanyEntity.headquarters = put.headquarters
            storedCompanyEntity.headquartersPostalCode = put.headquartersPostalCode
            storedCompanyEntity.sector = put.sector
            storedCompanyEntity.sectorCodeWz = put.sectorCodeWz
            storedCompanyEntity.countryCode = put.countryCode
            storedCompanyEntity.website = put.website
            storedCompanyEntity.isTeaserCompany = put.isTeaserCompany ?: false
            storedCompanyEntity.parentCompanyLei = put.parentCompanyLei
            replaceNonIsinIdentifers(storedCompanyEntity = storedCompanyEntity, identifierMap = put.identifiers)
            replaceIsinIdentifiers(storedCompanyEntity = storedCompanyEntity, identifierMap = put.identifiers)
        }

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
         * Asserts that there are no identifiers in use contained in the [identifierMap]. Only checks non-ISIN identifiers by default.
         * Set [isinMode] to true to only check for ISIN identifiers.
         *
         * @param identifierMap contains the identifiers to check for duplicates
         * @param isinMode controls if ISIN identifiers or non-ISIN identifiers are checked
         */
        private fun assertNoDuplicateIdentifiersExist(
            identifierMap: Map<IdentifierType, List<String>>,
            isinMode: Boolean = false,
        ) {
            val duplicates = mutableListOf<CompanyIdentifierEntity>()
            if (isinMode) {
                val isins = identifierMap[IdentifierType.Isin] ?: emptyList()
                duplicates.addAll(isinLeiRepository.findAllById(isins).map { it.toCompanyIdentifierEntity() })
            } else {
                duplicates.addAll(
                    companyIdentifierRepositoryInterface.findAllById(
                        identifierMap.flatMap { identifierPair ->
                            identifierPair.value.map {
                                CompanyIdentifierEntityId(
                                    identifierType = identifierPair.key,
                                    identifierValue = it,
                                )
                            }
                        },
                    ),
                )
            }

            if (duplicates.isNotEmpty()) {
                throw DuplicateIdentifierApiException(duplicates)
            }
        }

        /**
         * Creates and associates non-ISIN identifiers with the given [storedCompanyEntity].
         * If ISIN identifiers are present in the [identifierMap], they are ignored.
         *
         * @param storedCompanyEntity the company entity to associate the identifiers with
         * @param identifierMap a map of identifier types to their values
         */
        private fun createAndAssociateNonIsinIdentifiers(
            storedCompanyEntity: StoredCompanyEntity,
            identifierMap: Map<IdentifierType, List<String>>,
        ): List<CompanyIdentifierEntity> {
            assertNoDuplicateIdentifiersExist(identifierMap)

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

            return companyIdentifierRepositoryInterface
                .saveAllAndFlush(newNonIsinIdentifiers)
                .toList()
        }

        /**
         * Creates and associates ISIN identifiers with the given [storedCompanyEntity].
         * If the [storedCompanyEntity] has a LEI it is recorded as well.
         *
         * @param storedCompanyEntity the company entity to associate the ISIN identifiers with
         * @param identifierMap a map of identifier types to their values
         */
        private fun createAndAssociateIsinIdentifiers(
            storedCompanyEntity: StoredCompanyEntity,
            identifierMap: Map<IdentifierType, List<String>>,
        ) {
            val isins = identifierMap[IdentifierType.Isin]
            val lei = storedCompanyEntity.identifiers.find { it.identifierType == IdentifierType.Lei }?.identifierValue
            if (isins.isNullOrEmpty()) return
            assertNoDuplicateIdentifiersExist(identifierMap = identifierMap, isinMode = true)

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
         * Replaces the non-ISIN identifiers of the given [storedCompanyEntity] with the identifiers from the [identifierMap].
         * If [identifierMap] is empty, all existing non-ISIN identifiers are deleted.
         *
         * @param storedCompanyEntity the company entity whose non-ISIN identifiers are to be replaced
         * @param identifierMap a map of identifier types to their new values
         */
        private fun replaceNonIsinIdentifers(
            storedCompanyEntity: StoredCompanyEntity,
            identifierMap: Map<IdentifierType, List<String>>,
        ) {
            companyIdentifierRepositoryInterface.deleteAllByCompany(storedCompanyEntity)
            storedCompanyEntity.identifiers = mutableListOf()
            storedCompanyEntity.identifiers.addAll(createAndAssociateNonIsinIdentifiers(storedCompanyEntity, identifierMap))
        }

        /**
         * Replaces the ISIN identifiers of the given [storedCompanyEntity] with the identifiers from the [identifierMap].
         * If [identifierMap] does not contain any ISINs, all existing ISIN identifiers are deleted.
         *
         * @param storedCompanyEntity the company entity whose ISIN identifiers are to be replaced
         * @param identifierMap a map of identifier types to their new values
         */
        private fun replaceIsinIdentifiers(
            storedCompanyEntity: StoredCompanyEntity,
            identifierMap: Map<IdentifierType, List<String>>,
        ) {
            isinLeiRepository.deleteAllByCompany(storedCompanyEntity)
            val newIsins = identifierMap[IdentifierType.Isin]
            val lei = storedCompanyEntity.identifiers.find { it.identifierType == IdentifierType.Lei }?.identifierValue
            if (newIsins.isNullOrEmpty()) return
            createAndAssociateIsinIdentifiers(
                storedCompanyEntity = storedCompanyEntity,
                identifierMap =
                    mapOf(
                        IdentifierType.Isin to newIsins,
                        IdentifierType.Lei to if (lei.isNullOrEmpty()) emptyList() else listOf(lei),
                    ),
            )
        }

        /**
         * Updates the non-ISIN identifiers of the given [storedCompanyEntity] based on the provided [identifierMap] map.
         * Existing identifiers of each type are removed before adding the new ones. The in memory entity is updated as well.
         *
         * @param storedCompanyEntity the company entity whose identifiers are to be updated
         * @param identifierMap a map of identifier types to their new values
         */
        private fun updateNonIsinIdentifiers(
            storedCompanyEntity: StoredCompanyEntity,
            identifierMap: Map<IdentifierType, List<String>>,
        ) {
            identifierMap.forEach { identifierType, _ ->
                if (identifierType == IdentifierType.Isin) return@forEach
                storedCompanyEntity.identifiers.removeIf { it.identifierType == identifierType }
                companyIdentifierRepositoryInterface.deleteAllByCompanyAndIdentifierType(storedCompanyEntity, identifierType)
            }
            storedCompanyEntity.identifiers.addAll(
                createAndAssociateNonIsinIdentifiers(storedCompanyEntity = storedCompanyEntity, identifierMap = identifierMap),
            )
        }
    }
