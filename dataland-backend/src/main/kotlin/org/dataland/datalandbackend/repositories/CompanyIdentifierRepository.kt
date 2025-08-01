package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.CompanyIdentifierEntityId
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/**
 * A JPA interface for accessing the CompanyIdentifier Entity
 */
interface CompanyIdentifierRepository : JpaRepository<CompanyIdentifierEntity, CompanyIdentifierEntityId> {
    /**
     * Auto-Generated function to delete all identifiers of a specific type belonging to a company
     */
    fun deleteAllByCompanyAndIdentifierType(
        company: StoredCompanyEntity,
        identifierType: IdentifierType,
    )

    /**
     * Auto-Generated function to delete all identifiers belonging to a company
     */
    fun deleteAllByCompany(company: StoredCompanyEntity)

    /**
     * Retrieve an entry based on the identifier value
     */
    fun getFirstByIdentifierValueIs(identifierValue: String): CompanyIdentifierEntity?

    /**
     * Retrieve existing entries for a given [identifierType] that match any of the given [identifierValues]
     * and are not associated with the [excludedCompany]
     *
     * @param identifierType the type of identifier to search for
     * @param identifierValues a list of identifier values to match against
     * @param excludedCompany the company to exclude from the search results
     * @return A list of `CompanyIdentifierEntity` that match the criteria
     */
    fun findByIdentifierTypeIsAndIdentifierValueInAndCompanyIsNot(
        identifierType: IdentifierType,
        identifierValues: List<String>,
        excludedCompany: StoredCompanyEntity,
    ): List<CompanyIdentifierEntity>

    /**
     * Auto generated function to obtain all company identifiers of a certain type given a collection of companies.
     */
    fun findCompanyIdentifierEntitiesByCompanyInAndIdentifierTypeIs(
        companies: Collection<StoredCompanyEntity>,
        identifierType: IdentifierType,
    ): MutableList<CompanyIdentifierEntity>

    /**
     * Finds a `CompanyIdentifierEntity` based on the identifier ID
     *
     * @param CompanyIdentifierEntityId The value of the identifier to search for.
     * @return An `Optional` containing the `CompanyIdentifierEntity` matching the given identifier value and type,
     * or empty if no match is found.
     */
    override fun findById(companyIdentifierEntityId: CompanyIdentifierEntityId): Optional<CompanyIdentifierEntity>
}
