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
    @ElementCollection
    @Column(name = "company_email_suffixes")
    @OrderBy("asc")
    var emailSuffixes: List<String>?,
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
                    emailSuffixes = emailSuffixes,
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
     * Calls setter on newValue as long as newValue is not null.
     * @param newValue the new value to set
     * @param setter the setter function to call
     */
    fun <T> updateIfNotNull(
        newValue: T?,
        setter: (T) -> Unit,
    ) {
        newValue?.let { setter(it) }
    }

    /**
     * Updates this [StoredCompanyEntity] according to the contant of the applied [CompanyInformationPatch].
     * @param patch the [CompanyInformationPatch] containing the new values to apply.
     */
    fun applyPatch(
        storedCompanyEntity: StoredCompanyEntity,
        patch: CompanyInformationPatch,
    ) {
        updateIfNotNull(patch.companyName) { storedCompanyEntity.companyName = it }
        updateIfNotNull(patch.companyAlternativeNames) { storedCompanyEntity.companyAlternativeNames = it.toMutableList() }
        updateIfNotNull(patch.companyContactDetails) { storedCompanyEntity.companyContactDetails = it.toMutableList() }
        updateIfNotNull(patch.companyLegalForm) { storedCompanyEntity.companyLegalForm = it }
        updateIfNotNull(patch.headquarters) { storedCompanyEntity.headquarters = it }
        updateIfNotNull(patch.headquartersPostalCode) { storedCompanyEntity.headquartersPostalCode = it }
        updateIfNotNull(patch.sector) { storedCompanyEntity.sector = it }
        updateIfNotNull(patch.sectorCodeWz) { storedCompanyEntity.sectorCodeWz = it }
        updateIfNotNull(patch.countryCode) { storedCompanyEntity.countryCode = it }
        updateIfNotNull(patch.website) { storedCompanyEntity.website = it }
        updateIfNotNull(patch.isTeaserCompany) { storedCompanyEntity.isTeaserCompany = it }
        updateIfNotNull(patch.parentCompanyLei) { storedCompanyEntity.parentCompanyLei = it }
        updateIfNotNull(patch.emailSuffixes) { storedCompanyEntity.emailSuffixes = it }

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
