package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.StoredCompanyStockIndexEntity
import org.dataland.datalandbackend.entities.StoredCompanyStockIndexEntityId
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for accessing the StoredCompanyStockIndex Entity
 */
interface StoredCompanyStockIndexRepository :
    JpaRepository<StoredCompanyStockIndexEntity, StoredCompanyStockIndexEntityId>
