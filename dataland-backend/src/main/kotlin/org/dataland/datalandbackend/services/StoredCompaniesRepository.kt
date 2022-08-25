package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.StoredCompany
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring scans all JPA repository interfaces when the application is booted.
 * This interface here exists to trigger the creation of a table which can hold the content of StoredCompany
 * objects.
 */
interface StoredCompaniesRepository : JpaRepository<StoredCompany, String> {
    // non-empty
}
