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
    fun findAllByLei(
        lei: String,
        pageable: Pageable,
    ): Page<IsinLeiEntity>

    fun findByCompanyId(
        companyId: String,
        pageable: Pageable,
    ): Page<IsinLeiEntity>
}
