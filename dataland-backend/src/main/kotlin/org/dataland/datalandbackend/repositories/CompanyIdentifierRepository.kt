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
    fun getByIdentifierValueIs(identifierValue: String): CompanyIdentifierEntity?
}
