package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.StoredCompany
import org.springframework.data.jpa.repository.JpaRepository

interface StoredCompaniesRepository : JpaRepository<StoredCompany?, String?> {
}