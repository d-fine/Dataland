package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * A JPA Repository to match an ISIN to a corresponding LEI.
 */
@Repository
interface IsinLeiRepository : JpaRepository<IsinLeiEntity, String> {
    /**
     * Deletes all ISIN-LEI mappings for a given company ID.
     *
     * @param companyId The ID of the company whose mappings should be deleted.
     */
    fun deleteAllByCompany(companyId: StoredCompanyEntity)

    /**
     * Finds an ISIN-LEI entity by its ISIN.
     *
     * @param isin The ISIN to search for.
     * @return The corresponding `IsinLeiEntity`, or null if not found.
     */
    fun findByIsin(isin: String): IsinLeiEntity?

    /**
     * Finds ISIN-LEI entities associated to a given company, with pagination support.
     *
     * @param storedCompanyEntity The stored company to search for.
     * @param pageable The pagination information.
     * @return A `Page` of `IsinLeiEntity` matching the provided stored company.
     */
    fun findByCompany(
        storedCompanyEntity: StoredCompanyEntity,
        pageable: Pageable,
    ): Page<IsinLeiEntity>

    /**
     * Finds all ISIN-LEI entities by a specific LEI.
     *
     * @param lei The LEI to search for.
     * @return A list of `IsinLeiEntity` matching the provided LEI.
     */
    fun findAllByLei(lei: String): List<IsinLeiEntity>
}
