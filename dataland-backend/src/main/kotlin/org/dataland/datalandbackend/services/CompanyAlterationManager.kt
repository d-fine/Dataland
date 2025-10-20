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
            val newCompany = createStoredCompanyEntityWithoutForeignReferences(companyId, companyInformation)

            val nonIsinIdentifiers = createNonIsinIdentifiers(newCompany, companyInformation.identifiers)
            newCompany.addIdentifiers(nonIsinIdentifiers)
            val savedCompany = storedCompanyRepository.save(newCompany)
            createAndAssociateIsinIdentifiers(savedCompany, companyInformation.identifiers)

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
            assertNoDuplicateIdentifiersExist(patch.identifiers ?: emptyMap(), storedCompanyEntity)
            storedCompanyEntity.applyPatch(storedCompanyEntity, patch)
            val savedCompany = storedCompanyRepository.save(storedCompanyEntity)

            val patchedIdentifiers = patch.identifiers ?: emptyMap()
            if (patchedIdentifiers[IdentifierType.Isin] != null) {
                replaceIsinIdentifiers(
                    storedCompanyEntity = savedCompany,
                    identifierMap = patchedIdentifiers,
                )
            }

            return savedCompany
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
            assertNoDuplicateIdentifiersExist(companyInformation.identifiers, storedCompanyEntity)
            storedCompanyEntity.applyPut(companyInformation)
            val savedCompany = storedCompanyRepository.save(storedCompanyEntity)
            replaceIsinIdentifiers(storedCompanyEntity = savedCompany, identifierMap = companyInformation.identifiers)

            return savedCompany
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
                    fiscalYearEnd = companyInformation.fiscalYearEnd,
                    reportingPeriodShift = companyInformation.reportingPeriodShift,
                    sector = companyInformation.sector,
                    sectorCodeWz = companyInformation.sectorCodeWz,
                    identifiers = mutableListOf(),
                    dataRegisteredByDataland = mutableListOf(),
                    countryCode = companyInformation.countryCode,
                    isTeaserCompany = companyInformation.isTeaserCompany ?: false,
                    website = companyInformation.website,
                    parentCompanyLei = companyInformation.parentCompanyLei,
                    associatedSubdomains = companyInformation.associatedSubdomains,
                )

            return newCompanyEntity
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
            storedCompanyEntity: StoredCompanyEntity,
            isinMode: Boolean = false,
        ) {
            val duplicates = mutableListOf<CompanyIdentifierEntity>()
            if (isinMode) {
                val isins = identifierMap[IdentifierType.Isin] ?: emptyList()
                duplicates.addAll(isinLeiRepository.findAllById(isins).map { it.toCompanyIdentifierEntity() })
            } else {
                duplicates.addAll(
                    companyIdentifierRepositoryInterface
                        .findAllById(
                            identifierMap.flatMap { identifierPair ->
                                identifierPair.value.map {
                                    CompanyIdentifierEntityId(
                                        identifierType = identifierPair.key,
                                        identifierValue = it,
                                    )
                                }
                            },
                        ).filter { it.company != storedCompanyEntity },
                )
            }

            if (duplicates.isNotEmpty()) {
                throw DuplicateIdentifierApiException(duplicates)
            }
        }

        /**
         * Creates and associates non-ISIN identifiers
         * If ISIN identifiers are present in the [identifierMap], they are ignored.
         *
         * @param storedCompanyEntity the company entity to associate the identifiers with
         * @param identifierMap a map of identifier types to their values
         */
        private fun createNonIsinIdentifiers(
            storedCompanyEntity: StoredCompanyEntity,
            identifierMap: Map<IdentifierType, List<String>>,
        ): List<CompanyIdentifierEntity> {
            assertNoDuplicateIdentifiersExist(identifierMap, storedCompanyEntity)

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

            return newNonIsinIdentifiers
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
            assertNoDuplicateIdentifiersExist(
                identifierMap = identifierMap, isinMode = true,
                storedCompanyEntity = storedCompanyEntity,
            )

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
    }
