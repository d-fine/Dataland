package org.dataland.datalandbackend.entities

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import org.dataland.datalandbackend.interfaces.ApiModelConversion
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
import org.dataland.datalandbackend.model.enums.company.IdentifierType

/**
 * The entity storing data regarding a company stored in dataland
 */
@Entity
@Table(name = "stored_companies")
data class StoredCompanyEntity(
    @Id
    @Column(name = "company_id")
    val companyId: String,
    @Column(name = "company_name", length = 1000)
    var companyName: String,
    @ElementCollection
    @Column(name = "company_alternative_names", length = 1000)
    @OrderBy("asc")
    var companyAlternativeNames: List<String>?,
    @ElementCollection
    @Column(name = "company_contact_details")
    @OrderBy("asc")
    var companyContactDetails: List<String>?,
    @Column(name = "company_legal_form")
    var companyLegalForm: String?,
    @Column(name = "headquarters")
    var headquarters: String,
    @Column(name = "headquarters_postal_code")
    var headquartersPostalCode: String?,
    @Column(name = "sector")
    var sector: String?,
    @Column(name = "sector_code_wz")
    var sectorCodeWz: String?,
    @OneToMany(mappedBy = "company", orphanRemoval = true, cascade = [CascadeType.ALL])
    @JsonManagedReference
    var identifiers: MutableList<CompanyIdentifierEntity>,
    @Column(name = "parent_company_lei")
    var parentCompanyLei: String?,
    @OneToMany(mappedBy = "company")
    val dataRegisteredByDataland: MutableList<DataMetaInformationEntity>,
    @Column(name = "country_code")
    var countryCode: String,
    @Column(name = "is_teaser_company")
    var isTeaserCompany: Boolean,
    @Column(name = "website")
    var website: String?,
) : ApiModelConversion<StoredCompany> {
    @JsonValue
    override fun toApiModel(): StoredCompany {
        val identifierMap = createIdentifierMap()

        return StoredCompany(
            companyId = companyId,
            companyInformation =
                CompanyInformation(
                    companyName = companyName,
                    companyAlternativeNames = companyAlternativeNames,
                    companyLegalForm = companyLegalForm,
                    companyContactDetails = companyContactDetails,
                    headquarters = headquarters,
                    headquartersPostalCode = headquartersPostalCode,
                    sector = sector,
                    sectorCodeWz = sectorCodeWz,
                    identifiers = identifierMap,
                    countryCode = countryCode,
                    isTeaserCompany = isTeaserCompany,
                    website = website,
                    parentCompanyLei = parentCompanyLei,
                ),
            dataRegisteredByDataland = dataRegisteredByDataland.map { it.toApiModel() }.toMutableList(),
        )
    }

    private fun createIdentifierMap(): MutableMap<IdentifierType, MutableList<String>> {
        val identifierMap = mutableMapOf<IdentifierType, MutableList<String>>()
        for (identifierType in IdentifierType.entries) {
            identifierMap[identifierType] = mutableListOf()
        }

        for (identifier in identifiers) {
            val entry = identifierMap[identifier.identifierType]
            requireNotNull(entry)
            entry.add(identifier.identifierValue)
        }

        identifierMap.values.forEach { it.sort() }
        return identifierMap
    }

    /**
     * Adds a list of identifiers to the company entity.
     * The identifiers will be linked to this company entity.
     *
     * @param identifiers the list of identifiers to add
     */
    fun addIdentifiers(identifiers: List<CompanyIdentifierEntity>) {
        this.identifiers.addAll(identifiers)
        for (identifierEntity in identifiers) {
            identifierEntity.company = this
        }
    }

    /**
     * Clears all identifiers from the company entity.
     */
    fun removeIdentifiers(identifiers: List<CompanyIdentifierEntity>) {
        this.identifiers.removeAll(identifiers)
    }

    /**
     * Clears all identifiers from the company entity.
     */
    fun clearIdentifiers() {
        this.identifiers.clear()
    }

    /**
     * Replaces the current identifiers with a new list of identifiers.
     * This will clear existing identifiers and add the new ones.
     *
     * @param identifiers the new list of identifiers to set
     */
    fun replaceIdentifiers(identifiers: List<CompanyIdentifierEntity>) {
        clearIdentifiers()
        addIdentifiers(identifiers)
    }

    /**
     * Updates this [StoredCompanyEntity] according to the contant of the applied [CompanyInformationPatch].
     * @param patch the [CompanyInformationPatch] containing the new values to apply.
     */
    fun applyPatch(
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
        this.removeIdentifiers(findNonIsinIdentifiersToRemove(patchedIdentifiers))
        this.addIdentifiers(
            findNonIsinIdentifiers(patchedIdentifiers),
        )
    }

    /**
     * Updates the non-ISIN identifiers of the given [storedCompanyEntity] based on the provided [identifierMap] map.
     * Existing identifiers of each type are removed before adding the new ones. The in memory entity is updated as well.
     *
     * @param storedCompanyEntity the company entity whose identifiers are to be updated
     * @param identifierMap a map of identifier types to their new values
     */
    private fun findNonIsinIdentifiersToRemove(identifierMap: Map<IdentifierType, List<String>>): List<CompanyIdentifierEntity> {
        val nonIsinIdentifiersToRemove = mutableListOf<CompanyIdentifierEntity>()
        identifierMap.forEach { identifierType, _ ->
            if (identifierType == IdentifierType.Isin) return@forEach
            nonIsinIdentifiersToRemove.addAll(this.identifiers.filter { it.identifierType == identifierType })
        }
        return nonIsinIdentifiersToRemove
    }

    /**
     * Updates this [StoredCompanyEntity] according to the content of the applied [CompanyInformation].
     * @param put the [CompanyInformation] containing the new values to apply.
     */
    fun applyPut(put: CompanyInformation) {
        val newNonIsinIdentifiers =
            findNonIsinIdentifiers(put.identifiers)

        this.companyName = put.companyName
        this.companyAlternativeNames = put.companyAlternativeNames?.toMutableList()
        this.companyContactDetails = put.companyContactDetails?.toMutableList()
        this.companyLegalForm = put.companyLegalForm
        this.headquarters = put.headquarters
        this.headquartersPostalCode = put.headquartersPostalCode
        this.sector = put.sector
        this.sectorCodeWz = put.sectorCodeWz
        this.countryCode = put.countryCode
        this.website = put.website
        this.isTeaserCompany = put.isTeaserCompany ?: false
        this.parentCompanyLei = put.parentCompanyLei
        this.replaceIdentifiers(newNonIsinIdentifiers)
    }

    private fun findNonIsinIdentifiers(identifierMap: Map<IdentifierType, List<String>>): List<CompanyIdentifierEntity> =
        identifierMap
            .flatMap { identifierPair ->
                identifierPair.value.map {
                    CompanyIdentifierEntity(
                        identifierType = identifierPair.key, identifierValue = it,
                        company = this, isNew = true,
                    )
                }
            }.distinct()
            .filter { it.identifierType != IdentifierType.Isin }
}
