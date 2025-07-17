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

        private fun assertNoDuplicateNonIsinIdentifiersExist(
            storedCompanyEntity: StoredCompanyEntity,
            identifierMap: Map<IdentifierType, List<String>>,
            ignoreSpecifiedCompany: Boolean = false,
        ) {
            val duplicateNonIsinIdentifiers =
                companyIdentifierRepositoryInterface.findAllById(
                    identifierMap.flatMap { identifierPair ->
                        identifierPair.value.map {
                            CompanyIdentifierEntityId(
                                identifierType = identifierPair.key,
                                identifierValue = it,
                            )
                        }
                    },
                )

            val violatingNonIsinIdentifiers =
                if (ignoreSpecifiedCompany) {
                    duplicateNonIsinIdentifiers.filter { it.company?.companyId != storedCompanyEntity.companyId }
                } else {
                    duplicateNonIsinIdentifiers
                }

            if (violatingNonIsinIdentifiers.isNotEmpty()) {
                throw DuplicateIdentifierApiException(violatingNonIsinIdentifiers)
            }
        }

        private fun assertNoDuplicateIsinIdentifiersExist(
            storedCompanyEntity: StoredCompanyEntity,
            identifierMap: Map<IdentifierType, List<String>>,
            ignoreSpecifiedCompany: Boolean = false,
        ) {
            val specifiedIsins = identifierMap[IdentifierType.Isin]
            if (specifiedIsins == null) return
            val duplicateIsinIdentifiers =
                isinLeiRepository.findAllByIsinIn(specifiedIsins)

            val violatingIsinIdentifiers =
                if (ignoreSpecifiedCompany) {
                    duplicateIsinIdentifiers.filter { it.company != storedCompanyEntity }
                } else {
                    duplicateIsinIdentifiers
                }

            if (violatingIsinIdentifiers.isNotEmpty()) {
                throw DuplicateIdentifierApiException(
                    violatingIsinIdentifiers.map {
                        CompanyIdentifierEntity(
                            identifierType = IdentifierType.Isin,
                            identifierValue = it.isin,
                            company = storedCompanyEntity,
                        )
                    },
                )
            }
        }

        private fun assertNoDuplicateSpecifiedLeisExist(identifierMap: Map<IdentifierType, List<String>>) {
            val specifiedLeis = identifierMap[IdentifierType.Lei]?.distinct()
            if (specifiedLeis != null && specifiedLeis.size > 1) {
                throw InvalidInputApiException(
                    summary = "Duplicate LEIs detected.",
                    message = "There is more than one distinct entry in your specified list of LEIs.",
                )
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
            identifierMap: Map<IdentifierType, List<String>>,
        ): List<CompanyIdentifierEntity> {
            val leiOrNull = identifierMap[IdentifierType.Lei]?.firstOrNull()

            val isinLeiEntities =
                identifierMap[IdentifierType.Isin]?.map {
                    IsinLeiEntity(
                        company = storedCompanyEntity,
                        isin = it,
                        lei = leiOrNull,
                    )
                }

            if (isinLeiEntities == null) return emptyList()

            return isinLeiRepository.saveAllAndFlush(isinLeiEntities).toList().map {
                CompanyIdentifierEntity(
                    identifierType = IdentifierType.Isin,
                    identifierValue = it.isin,
                    company = storedCompanyEntity,
                    isNew = true,
                )
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
            assertNoDuplicateNonIsinIdentifiersExist(savedCompany, companyInformation.identifiers)
            assertNoDuplicateSpecifiedLeisExist(companyInformation.identifiers)
            assertNoDuplicateIsinIdentifiersExist(savedCompany, companyInformation.identifiers)
            val nonIsinIdentifiers = createAndAssociateNonIsinIdentifiers(savedCompany, companyInformation.identifiers)
            val isinIdentifiers = createAndAssociateIsinIdentifiers(savedCompany, companyInformation.identifiers)
            val allIdentifiers = nonIsinIdentifiers + isinIdentifiers
            savedCompany.identifiers = allIdentifiers.toMutableList()
            logger.info("Company ${companyInformation.companyName} with ID $companyId saved to database.")
            return savedCompany
        }

        private fun replaceNonIsinCompanyIdentifiers(
            storedCompanyEntity: StoredCompanyEntity,
            identifierType: IdentifierType,
            newIdentifiers: List<String>,
        ) {
            if (identifierType == IdentifierType.Isin) return
            companyIdentifierRepositoryInterface.deleteAllByCompanyAndIdentifierType(
                storedCompanyEntity,
                identifierType,
            )
            createAndAssociateNonIsinIdentifiers(storedCompanyEntity, mapOf(identifierType to newIdentifiers))
        }

        private fun replaceIsinLeiEntities(
            storedCompanyEntity: StoredCompanyEntity,
            newIdentifiers: List<String>,
            leiToUse: String?,
        ) {
            isinLeiRepository.deleteAllByCompany(storedCompanyEntity)
            val identifierMapToUse =
                mutableMapOf(
                    IdentifierType.Isin to newIdentifiers,
                )
            if (leiToUse != null) identifierMapToUse[IdentifierType.Lei] = listOf(leiToUse)
            createAndAssociateIsinIdentifiers(
                storedCompanyEntity,
                identifierMapToUse,
            )
        }

        /**
         * Method to patch the information of a company.
         * @param companyId the id of the company to patch
         * @param patch the patch to apply to the company
         * @return the updated company information object
         */
        @Suppress("CyclomaticComplexMethod")
        @Transactional
        fun patchCompany(
            companyId: String,
            patch: CompanyInformationPatch,
        ): StoredCompanyEntity {
            val storedCompanyEntity = companyQueryManager.getCompanyById(companyId)
            assertNoDuplicateNonIsinIdentifiersExist(storedCompanyEntity, patch.identifiers ?: emptyMap(), true)
            assertNoDuplicateSpecifiedLeisExist(patch.identifiers ?: emptyMap())
            assertNoDuplicateIsinIdentifiersExist(storedCompanyEntity, patch.identifiers ?: emptyMap(), true)
            logger.info("Patching Company ${storedCompanyEntity.companyName} with ID $companyId")
            patch.companyName?.let { storedCompanyEntity.companyName = it }
            patch.companyAlternativeNames?.let { storedCompanyEntity.companyAlternativeNames = it }
            patch.companyContactDetails?.let { storedCompanyEntity.companyContactDetails = it }
            patch.companyLegalForm?.let { storedCompanyEntity.companyLegalForm = it }
            patch.headquarters?.let { storedCompanyEntity.headquarters = it }
            patch.headquartersPostalCode?.let { storedCompanyEntity.headquartersPostalCode = it }
            patch.sector?.let { storedCompanyEntity.sector = it }
            patch.sectorCodeWz?.let { storedCompanyEntity.sectorCodeWz = it }
            patch.countryCode?.let { storedCompanyEntity.countryCode = it }
            patch.website?.let { storedCompanyEntity.website = it }
            patch.isTeaserCompany?.let { storedCompanyEntity.isTeaserCompany = it }
            patch.parentCompanyLei?.let { storedCompanyEntity.parentCompanyLei = it }

            if (patch.identifiers == null) return storedCompanyRepository.save(storedCompanyEntity)

            for (keypair in patch.identifiers) {
                if (keypair.key == IdentifierType.Isin) continue
                replaceNonIsinCompanyIdentifiers(
                    storedCompanyEntity = storedCompanyEntity,
                    identifierType = keypair.key,
                    newIdentifiers = keypair.value.distinct(),
                )
            }

            val newIsinIdentifiers = patch.identifiers[IdentifierType.Isin]
            val leiAfterPatch =
                if (patch.identifiers[IdentifierType.Lei] != null) {
                    patch.identifiers[IdentifierType.Lei]?.firstOrNull()
                } else {
                    storedCompanyEntity.identifiers.find { it.identifierType == IdentifierType.Lei }?.identifierValue
                }

            if (newIsinIdentifiers != null) {
                replaceIsinLeiEntities(
                    storedCompanyEntity = storedCompanyEntity,
                    newIdentifiers = newIsinIdentifiers,
                    leiToUse = leiAfterPatch,
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
            isinLeiRepository.deleteAllByCompany(
                storedCompanyEntity,
            )
            createAndAssociateNonIsinIdentifiers(storedCompanyEntity, companyInformation.identifiers)
            createAndAssociateIsinIdentifiers(storedCompanyEntity, companyInformation.identifiers)

            return storedCompanyRepository.save(storedCompanyEntity)
        }
    }
