package org.dataland.datalanduserservice.repository

import org.dataland.datalanduserservice.entity.IncompleteCompanyInformationEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * A JPA repository for storing and retrieving the human log of incomplete company information
 */
interface IncompleteCompanyInformationRepository : JpaRepository<IncompleteCompanyInformationEntity, String>
