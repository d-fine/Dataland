package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.CompanyIdentifierEntityId
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA interface for accessing the CompanyIdentifier Entity
 */
interface CompanyIdentifierRepository : JpaRepository<CompanyIdentifierEntity, CompanyIdentifierEntityId> {
    /**
     * Retrieve an entry based on the identifier value
     */
    fun getFirstByIdentifierValueIs(identifierValue: String): CompanyIdentifierEntity?

    /**
     * Auto generated function to obtain all company identifiers of a certain type given a collection of companies.
     */
    fun findCompanyIdentifierEntitiesByCompanyInAndIdentifierTypeIs(
        companies: Collection<StoredCompanyEntity>,
        identifierType: IdentifierType,
    ): MutableList<CompanyIdentifierEntity>
}
