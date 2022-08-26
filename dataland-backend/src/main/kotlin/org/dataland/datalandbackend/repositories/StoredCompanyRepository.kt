package org.dataland.datalandbackend.repositories

import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StoredCompanyRepository : JpaRepository<StoredCompanyEntity, String>