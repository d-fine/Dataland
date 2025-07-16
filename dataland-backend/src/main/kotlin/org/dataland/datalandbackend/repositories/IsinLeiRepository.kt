package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.IsinLeiEntity
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
    fun deleteAllByCompanyId(companyId: String)

    /**
     * Finds an ISIN-LEI entity by its ISIN.
     *
     * @param isin The ISIN to search for.
     * @return The corresponding `IsinLeiEntity`, or null if not found.
     */
    fun findByIsin(isin: String): IsinLeiEntity?

    /**
     * Finds all ISIN-LEI entities by a list of ISINs.
     *
     * @param isins The list of ISINs to search for.
     * @return A list of `IsinLeiEntity` matching the provided ISINs.
     */
    fun findAllByIsinIn(isins: List<String>): List<IsinLeiEntity>

    /**
     * Finds all ISIN-LEI entities for a given company ID, with pagination support.
     *
     * @param companyId The ID of the company to search for.
     * @param pageable The pagination information.
     * @return A `Page` of `IsinLeiEntity` matching the provided company ID.
     */
    fun findByCompanyId(
        companyId: String,
        pageable: Pageable,
    ): Page<IsinLeiEntity>
}
