package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * A JPA Repository to match an ISIN to a corresponding LEI.
 */
@Repository
interface IsinLeiRepository : JpaRepository<IsinLeiEntity, String>
